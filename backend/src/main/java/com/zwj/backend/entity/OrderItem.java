package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("order_items")
public class OrderItem {
    @Id
    private Integer id;
    private Integer orderId;
    private Integer bookId;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非表字段，用于前端展示
    private transient Book book;
} 