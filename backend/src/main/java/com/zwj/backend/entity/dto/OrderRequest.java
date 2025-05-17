package com.zwj.backend.entity.dto;

import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items; // 订单项列表，每个订单项包含商品ID和数量
    private String paymentMethod; // 支付方式
    
    @Data
    public static class OrderItemRequest {
        private Long bookId; // 书本ID
        private Integer quantity; // 数量
    }
} 