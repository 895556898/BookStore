package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("order_addresses")
public class OrderAddress {
    @Id
    private Long id;
    private Long orderId;
    private String receiver;            // 收货人
    private String phone;               // 联系电话
    private String province;            // 省
    private String city;                // 市
    private String district;            // 区
    private String detailAddress;       // 详细地址
    private String zipCode;             // 邮政编码
    private Boolean isDefault;          // 是否默认地址 
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
} 