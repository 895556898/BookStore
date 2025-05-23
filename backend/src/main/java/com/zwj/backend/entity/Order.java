package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Table("orders")
public class Order {                     //订单
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private String status; // PENDING, PAID, CANCELLED
    private String remark; // 订单备注
    private String paymentMethod; // 支付方式
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非表字段，用于前端展示
    private transient List<OrderItem> orderItems;
    private transient User user;
    private transient OrderAddress address; // 订单地址
} 