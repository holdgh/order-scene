package com.logistics.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OrderTrack {
    private Long id;
    private String orderId;
    // 0待取件 1运输中 2派送中 3签收
    private Integer node;
    private String address;
    private Date createTime;
}
