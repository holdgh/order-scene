package com.logistics.cache;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.logistics.util.LogUtil;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 缓存同步监听器（核心）
@Component
public class CacheSyncListener implements MessageListener {

    @Resource
    private LoadingCache<String, Object> caffeineCache;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String orderId = new String(message.getBody());
        LogUtil.logger.info("收到缓存同步消息，清除本地缓存 orderId：{}", orderId);
        caffeineCache.invalidate("order:" + orderId);
    }
}
