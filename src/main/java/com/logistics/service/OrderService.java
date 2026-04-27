package com.logistics.service;

import com.logistics.entity.Order;
import com.logistics.entity.OrderTrack;

public interface OrderService {
    void createOrder(Order order);
    Order getOrder(String orderId);
    void addTrack(OrderTrack track);
}
