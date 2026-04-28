package com.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.entity.Idempotent;
import com.logistics.entity.Order;
import com.logistics.entity.OrderTrack;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    void insertOrder(Order order);
    Order selectByOrderId(String orderId);
    void insertTrack(OrderTrack track);
    void insertIdempotent(String businessId);
    Idempotent selectIdempotent(String businessId);
}
