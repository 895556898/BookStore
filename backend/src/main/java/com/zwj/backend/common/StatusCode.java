package com.zwj.backend.common;

import lombok.Getter;

@Getter
public enum StatusCode {
    REGISTER_SUCCESS(10000, "注册成功", "ok"),
    REGISTER_USERNAME_DUPLICATE(10001, "注册失败，用户已存在", "error"),
    ;


    final int code;
    final String msg;
    final String status;

    StatusCode(int code, String msg, String status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }
}
