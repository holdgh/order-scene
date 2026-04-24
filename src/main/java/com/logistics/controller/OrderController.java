package com.logistics.controller;

import com.logistics.entity.Order;
import com.logistics.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @PostMapping("/create")
    public String create(@RequestBody Order order) {
        order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        order.setStatus(0);
        orderService.createOrder(order);
        return "创建成功：" + order.getOrderId();
    }

    @GetMapping("/get")
    public Order get(String orderId) {
        return orderService.getOrder(orderId);
    }

    @PostMapping("/track")
    public String track(String orderId, String node) {
        orderService.addTrack(orderId, node);
        return "轨迹已上报";
    }
}