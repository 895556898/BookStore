package com.zwj.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    @Column(nullable = false, unique = true)
//    @NotBlank(
//            message = "密码不能为空"
//    )
//    @Length(
//            message = "用户名最少为{min}个字符"
//            , min = 6
//    )
    private String username;

//    @Column(nullable = false)
//    @NotBlank(
//            message = "密码不能为空"
//    )
//    @Length(
//            message = "密码最少为{min}个字符，最长为 {max}个字符"
//            , min = 6
//            , max = 30
//    )
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;
    private String role;                //用户类型

    public User(){
        id = 0;
        username = "";
        role = "user";
        phone = "";
    }

}
