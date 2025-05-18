package com.zwj.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Table("user")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private String phone;
    private String role;                //用户类型

    public User(){
        id = 0;
        username = "";
        role = "user";
        phone = "";
    }

}
