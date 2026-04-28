package com.logistics.entity;

//import com.baomidou.mybatisplus.annotations.TableField;
//import com.baomidou.mybatisplus.annotations.TableId;
//import com.baomidou.mybatisplus.annotations.TableName;
//import com.baomidou.mybatisplus.enums.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "order")
public class Order {

//    @TableId(value = "order_id", type = IdType.INPUT) // 显式绑定 order_id
    private String orderId;

//    @TableField(value = "user_id")
    private Long userId;

//    @TableField(value = "consignee")
    private String consignee;

//    @TableField(value = "phone")
    private String phone;

//    @TableField(value = "status")
    private Integer status;

//    @TableField(value = "create_time")
    private Date createTime;

//    @TableField(value = "update_time")
    private Date updateTime;
}
