package com.zwj.backend.entity.dto;

import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookDTO {
    @Id
    private long id;
    private String name;
    private String writer;
    private BigDecimal price;           //书本售价
    private BigDecimal cost;            //书本成本
    private int stock;                  //库存
    private String sort;                //分类
}
