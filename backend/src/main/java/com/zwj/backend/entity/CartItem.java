package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("cart_items")
public class CartItem {                 //购物车
    @Id
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非表字段，用于前端展示
    private transient Book book;
} 