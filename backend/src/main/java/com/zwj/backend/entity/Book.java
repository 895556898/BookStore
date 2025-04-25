package com.zwj.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "book")
public class Book {
    @Id
    private long id;
    private String name;
    private String writer;
    private BigDecimal price;           //书本售价
    private BigDecimal cost;            //书本成本
    private int stock;                  //库存
    private String sort;                //分类
}
