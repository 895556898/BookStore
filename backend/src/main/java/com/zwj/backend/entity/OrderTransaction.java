package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("order_transactions")
public class OrderTransaction {              //订单记录
    @Id
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private LocalDateTime transactionTime;
    private String status; // SUCCESS, FAILED
    private String paymentMethod;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非表字段，用于前端展示
    private transient Order order;
} 