package com.zwj.backend.entity.dto;

import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items; // 订单项列表，每个订单项包含商品ID和数量
    private String paymentMethod; // 支付方式
    private AddressInfo addressInfo; // 收货地址信息
    private String remark; // 订单备注
    
    @Data
    public static class OrderItemRequest {
        private Long bookId; // 书本ID
        private Integer quantity; // 数量
    }
    
    @Data
    public static class AddressInfo {
        private String receiver; // 收货人
        private String phone; // 电话
        private List<String> region; // 地区信息，如[省, 市, 区]
        private String detailAddress; // 详细地址
        private String zipCode; // 邮政编码
        private Boolean isDefault; // 是否默认地址
    }
} 