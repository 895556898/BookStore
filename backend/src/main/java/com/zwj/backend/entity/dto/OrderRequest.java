package com.zwj.backend.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<Integer> cartItemIds; // 购物车项ID列表，用于从购物车创建订单
    private String paymentMethod; // 支付方式
} 