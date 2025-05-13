package com.zwj.backend.entity.dto;

import lombok.Data;

import java.util.List;
import java.math.BigDecimal;
@Data
public class OrderRequest {
    private List<Long> cartItemIds; // 购物车项ID列表，用于从购物车创建订单
    private String paymentMethod; // 支付方式
    private BigDecimal totalAmount; // 总价
} 