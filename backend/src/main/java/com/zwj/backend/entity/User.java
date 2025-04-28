package com.zwj.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @NotBlank(
            message = "密码不能为空"
    )
    @Length(
            message = "用户名最少为{min}个字符"
            , min = 6
    )
    private String username;

    @Column(nullable = false)
    @NotBlank(
            message = "密码不能为空"
    )
    @Length(
            message = "密码最少为{min}个字符，最长为 {max}个字符"
            , min = 6
            , max = 30
    )
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;

}
