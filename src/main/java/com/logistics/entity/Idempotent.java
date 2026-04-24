package com.logistics.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Idempotent {
    private String businessId;
    private Date createTime;
}
