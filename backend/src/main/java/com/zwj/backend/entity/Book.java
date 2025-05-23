package com.zwj.backend.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Table(value = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;        // 书名
    private String writer;      // 作者
    private String isbn;        // ISBN号
    private String cover;       // 封面图片URL
    private BigDecimal price;   // 售价
    private BigDecimal cost;    // 成本
    private Integer stock;      // 库存
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_tag",
        joinColumns = @JoinColumn(name = "bid"),
        inverseJoinColumns = @JoinColumn(name = "tid")
    )
    private List<Tag> tag;      // 分类
    private String  createdBy;  // 创建者
    private int sales;          // 销量
    private Boolean status;     //图书状态（true为在售，false为下架）

    public Book() {
        title = "";
        writer = "";
        isbn = "";
        cover = "";
        price = new BigDecimal(0);
        cost = new BigDecimal(0);
        stock = 0;
        tag = new ArrayList<>();
        createdBy = "";
        sales = 0;
        status = true;
    }

    public Book(Long id, String title, String writer, String isbn, String cover, BigDecimal price, BigDecimal cost, Integer stock, List<Tag> tag, String createdBy, int sales, boolean status) {
        this.id = id;
        this.title = title;
        this.writer = writer;
        this.isbn = isbn;
        this.cover = cover;
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.tag = tag;
        this.createdBy = createdBy;
        this.sales = sales;
        this.status = status;
    }
}
