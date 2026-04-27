package com.logistics.service.impl;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.logistics.entity.Order;
import com.logistics.entity.OrderTrack;
import com.logistics.mapper.OrderMapper;
import com.logistics.mq.producer.KafkaProducer;
import com.logistics.service.OrderService;
import com.logistics.util.RedisPubSubUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private LoadingCache<String, Object> caffeineCache;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisPubSubUtil redisPubSubUtil;
    @Resource
    private KafkaProducer kafkaProducer;

    private static final String ORDER_KEY = "order:";

    @Override
    @Transactional
    public void createOrder(Order order) {
        orderMapper.insertOrder(order);
        // 延迟双删
        deleteCache(order.getOrderId());
        // 发布缓存同步消息（所有节点本地缓存失效）
        redisPubSubUtil.publishCacheSync(order.getOrderId());
    }

    @Override
    public Order getOrder(String orderId) {
        // 1.本地缓存
        Order order = (Order) caffeineCache.getIfPresent(ORDER_KEY + orderId);
        if (order != null) return order;

        // 2.Redis
        String json = stringRedisTemplate.opsForValue().get(ORDER_KEY + orderId);
        if (json != null) {
            order = JSONUtil.toBean(json, Order.class);
            caffeineCache.put(ORDER_KEY + orderId, order);
            return order;
        }

        // 3.DB
        order = orderMapper.selectByOrderId(orderId);
        if (order != null) {
            stringRedisTemplate.opsForValue().set(ORDER_KEY + orderId, JSONUtil.toJsonStr(order),
                    10, TimeUnit.MINUTES);
            caffeineCache.put(ORDER_KEY + orderId, order);
        }
        return order;
    }

    @Override
    public void addTrack(OrderTrack track) {
        kafkaProducer.sendTrack(track.getOrderId(), track.getNode(), track.getAddress());
    }

    private void deleteCache(String orderId) {
        stringRedisTemplate.delete(ORDER_KEY + orderId);
        caffeineCache.invalidate(ORDER_KEY + orderId);
        // 延迟双删
        executor.schedule(() -> {
            stringRedisTemplate.delete(ORDER_KEY + orderId);
            caffeineCache.invalidate(ORDER_KEY + orderId);
        }, 500, TimeUnit.MILLISECONDS);
    }

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
}
