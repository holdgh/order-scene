package com.logistics.mq.consumer;

import com.logistics.entity.OrderTrack;
import com.logistics.mapper.OrderMapper;
import com.logistics.util.IdempotentUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

// 有序幂等消费
@Component
public class TrackConsumer {
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private IdempotentUtil idempotentUtil;
    @Resource
    private RedissonClient redissonClient;

    /*标准的分布式幂等架构：预校验 → 加锁 → 二次校验 → 执行业务 → 标记完成*/
    @KafkaListener(topics = "track-topic", groupId = "track-group")
    public void consume(ConsumerRecord<String, String> record) {
        String msg = record.value();
        // 1. 解析消息（增加健壮性判断）
        String[] arr = msg.split("\\|");
        if (arr.length < 3) {
            System.out.println("消息格式错误：" + msg);
            return;
        }
        String orderId = arr[0];
        Integer node = Integer.valueOf(arr[1]);
        String address = arr[2];
        // 这里采用node.hashCode()作为业务唯一标识的组成部分存在问题：当node为字符串类型是，一旦出现hash冲突，则两条业务上不同的数据在这里会被认为是相同消息，导致数据丢失
        // String businessId = "track:" + orderId + ":" + node.hashCode();
        // 正确写法：采用node值作为业务唯一标识的组成部分，不管node什么类型【数字/字符串】，都可以标识业务数据的唯一性
        // 幂等性唯一标识核心原则：业务上不同的消息 → 生成的 businessId 必须不同
        String businessId = "track:" + orderId + ":" + node;
        // 唯一标识库去重校验【预校验：快速失败，过滤99%重复消息】
        if (idempotentUtil.isProcessed(businessId)) {
            System.out.println("重复消费，忽略 orderId=" + orderId);
            // TODO 待手动提交ack消息
            return;
        }
        // 获取分布式锁【并发场景的请求同步，也即并发去重：仅有一个请求获取锁，其他请求排队等待】
        // 锁粒度存在问题，轨迹业务数据与订单数据的关系为多对一，仅采用订单id作为锁key，会导致同一订单的不同轨迹互相阻塞【加锁的目的是：使得同一订单的同一轨迹并发请求互相阻塞】
        // RLock lock = redissonClient.getLock("lock:track:" + orderId);
        String lockKey = "lock:track:" + orderId + ":" + node;
        RLock lock = redissonClient.getLock(lockKey); // 获取分布式锁实例
        /*Redisson 只有 两种模式，互斥：
            模式 A：无参 lock ()
                锁 30 秒
                看门狗启动
                每 10 秒检查一次
                不足 20 秒就续期回 30 秒
                生产推荐
            模式 B：手动指定时间 lock (5s)
                看门狗不启动
                不续期
                时间到自动释放
                适合调试、短任务
        */
        /*
        底层lua脚本加锁逻辑：
        -- ======================== 【第一步：判断锁是否存在】 ========================
        -- 检查 KEYS[1]（锁的key）是否存在
        -- exists 命令：存在返回1，不存在返回0
        if (redis.call('exists', KEYS[1]) == 0) then

            -- 锁不存在 → 创建锁：使用 HASH 结构存储
            -- KEYS[1]        = 锁名称（lock:track:123:1）
            -- ARGV[2]        = 线程唯一标识（UUID:threadId）
            -- 1              = 重入次数（第一次加锁=1）
            redis.call('hset', KEYS[1], ARGV[2], 1);

            -- 设置锁的过期时间（防止死锁）
            -- ARGV[1]        = 锁有效期（默认 30000ms = 30s）
            redis.call('pexpire', KEYS[1], ARGV[1]);

            -- 返回 nil 给 Java 端 → 表示**加锁成功**
            return nil;
        end;

        -- ======================== 【第二步：锁已存在，判断是否是自己的锁】 ========================
        -- 判断当前线程是否已经持有这把锁（可重入）
        -- hexists：判断 hash 里是否存在指定 field（当前线程ID）
        if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then

            -- 是自己的锁 → 重入次数 +1
            redis.call('hincrby', KEYS[1], ARGV[2], 1);

            -- 重入后，重新刷新过期时间（续期）
            redis.call('pexpire', KEYS[1], ARGV[1]);

            -- 返回 nil → 重入加锁成功
            return nil;
        end;

        -- ======================== 【第三步：别人持有锁，返回锁的剩余有效期】 ========================
        -- 走到这里说明：
        -- 1. 锁已存在
        -- 2. 不是当前线程持有的
        -- 返回锁的剩余过期时间（毫秒），让Java端进行等待/重试
        return redis.call('pttl', KEYS[1]);

        lua脚本参数说明：
        KEYS[1]      = this.getName()           → 锁key：lock:track:orderId:node
        ARGV[1]      = internalLockLeaseTime    → 30000ms（30秒）
        ARGV[2]      = getLockName(threadId)    → UUID:线程ID
        */
        // lock.lock(); // 加锁【这里采用默认有效期30秒和看门狗自动续期机制，以保证消息消费可靠性。若手动设置过期时间，则存在消费尚未完毕时锁就被释放的可能】
        // 优化加锁写法
        // 加锁（带等待时间 + 持有时间，防止死锁）
        boolean lockSuccess;
        try {
            // 若开启看门狗【不设置锁的有效期时，默认开启看门狗，并使用默认有效期30秒】，则只有 JVM 进程死掉，看门狗才会消失，锁才会 30 秒后过期！
            // 关闭看门狗【防止因线程卡死导致的锁无限续期问题，类似死锁】，最大等待时间10秒【放置消费线程无限等待获取锁】，锁有效期固定15秒【基于消费逻辑常规执行耗时设置，锁的最大持有时间】
            lockSuccess = lock.tryLock(10, 15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("加锁被中断，orderId=" + orderId);
            Thread.currentThread().interrupt();
            return;
        }

        if (!lockSuccess) {
            System.out.println("获取锁失败，稍后重试：orderId=" + orderId);
            return;
        }
        try {
            // 二次唯一标识去重校验【通过第一次校验的并发请求，在这些请求获取锁后，再次执行唯一标识去重校验】
            if (idempotentUtil.isProcessed(businessId)) return;
            // 持久化轨迹数据
            OrderTrack track = new OrderTrack();
            track.setOrderId(orderId);
            track.setNode(node);
            track.setAddress(address);
            orderMapper.insertTrack(track);
            // TODO 同步更新订单状态
            idempotentUtil.markProcessed(businessId);

            System.out.println("轨迹消费成功 orderId=" + orderId + " node=" + node);
            // TODO 待手动提交ack消息
        } finally { //  只要服务进程没有死、没有宕机、没有被 kill、没有崩溃
            /*finally 绝对不执行的 2 种核心场景（必记）
                JVM 进程直接终止（最常见、最需要防范）：
                    服务被强制杀死（kill -9 命令）、容器被销毁、服务器断电 / 宕机
                    JVM 崩溃（如 OOM 内存溢出导致进程退出）
                    主动调用 System.exit (0)（生产代码严禁使用）
                    此时线程、看门狗均随进程死亡，finally 代码块无执行机会。
                    线程被暴力终止（已废弃，几乎不出现）
                调用线程的 stop () 方法（Java 已废弃，会强制终止线程，不执行任何收尾逻辑）：
                    生产环境不会用到，可忽略，但需知晓。
            */
            // TODO 待检查当前线程是否持有当前锁
            lock.unlock();
        }
    }
}
