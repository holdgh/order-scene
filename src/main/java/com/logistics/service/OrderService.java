package com.logistics.service;

import com.logistics.entity.Order;

public interface OrderService {
    void createOrder(Order order);
    Order getOrder(String orderId);
    void addTrack(String orderId, String node);
}
