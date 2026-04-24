package com.logistics.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OrderTrack {
    private Long id;
    private String orderId;
    private String node;
    private String address;
    private Date createTime;
}
