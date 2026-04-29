package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "t_idempotent")
public class Idempotent {
    private String businessId;
    private Date createTime;
}
