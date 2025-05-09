package com.zwj.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "book_tag")
public class BookTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "bid")
    private Long bid;
    
    @Column(name = "tid")
    private Long tid;
}
