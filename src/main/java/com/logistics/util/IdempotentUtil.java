package com.logistics.util;

import com.logistics.mapper.OrderMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 幂等校验
@Component
public class IdempotentUtil {
    @Resource
    private OrderMapper orderMapper;

    public boolean isProcessed(String businessId) {
        return orderMapper.selectIdempotent(businessId) != null;
    }

    public void markProcessed(String businessId) {
        orderMapper.insertIdempotent(businessId);
    }
}
