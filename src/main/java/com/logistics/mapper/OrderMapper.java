package com.logistics.mapper;

import com.logistics.entity.Idempotent;
import com.logistics.entity.Order;
import com.logistics.entity.OrderTrack;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    Order selectByOrderId(String orderId);
    void insertTrack(OrderTrack track);
    void insertIdempotent(String businessId);
    Idempotent selectIdempotent(String businessId);
}
