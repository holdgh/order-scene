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

    @KafkaListener(topics = "track-topic", groupId = "track-group")
    public void consume(ConsumerRecord<String, String> record) {
        String msg = record.value();
        String[] arr = msg.split("\\|");
        String orderId = arr[0];
        String node = arr[1];
        String businessId = "track:" + orderId + ":" + node.hashCode();

        if (idempotentUtil.isProcessed(businessId)) {
            System.out.println("重复消费，忽略 orderId=" + orderId);
            return;
        }

        RLock lock = redissonClient.getLock("lock:track:" + orderId);
        lock.lock(5, TimeUnit.SECONDS);
        try {
            if (idempotentUtil.isProcessed(businessId)) return;

            OrderTrack track = new OrderTrack();
            track.setOrderId(orderId);
            track.setNode(node);
            track.setAddress("模拟地址");
            orderMapper.insertTrack(track);
            idempotentUtil.markProcessed(businessId);

            System.out.println("轨迹消费成功 orderId=" + orderId + " node=" + node);
        } finally {
            lock.unlock();
        }
    }
}
