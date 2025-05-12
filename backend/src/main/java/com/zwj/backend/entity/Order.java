package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("orders")
public class Order {                     //订单 
    @Id
    private Integer id;
    private Integer userId;
    private BigDecimal totalAmount;
    private String status; // PENDING, PAID, CANCELLED
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非表字段，用于前端展示
    private transient List<OrderItem> orderItems;
    private transient User user;
} 