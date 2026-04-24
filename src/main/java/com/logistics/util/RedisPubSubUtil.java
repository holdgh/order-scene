package com.logistics.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 发送缓存同步消息
@Component
public class RedisPubSubUtil {

    /*
    为什么有两种 RedisTemplate？
    区别只有一个：
    key 和 value 的序列化方式不同！
        1. stringRedisTemplate
        key = String
        value = String
        序列化 = StringRedisSerializer
        存进去的内容 肉眼可见
        工作中 95% 场景用它
        2. redisTemplate<Object,Object>
        key = Object
        value = Object
        序列化 = JDK 序列化
        存进去是 二进制乱码
        不推荐直接使用
    */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void publishCacheSync(String orderId) {
        stringRedisTemplate.convertAndSend("cache:sync", orderId);
    }
}
