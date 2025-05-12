package com.zwj.backend.common;

import lombok.Getter;

@Getter
public enum StatusCode {
    REGISTER_SUCCESS(10000, "注册成功", "ok"),
    REGISTER_USERNAME_EXIST(10001, "注册失败，用户已存在", "error"),
    REGISTER_PHONE_EXIST(10002, "注册失败，手机号已存在", "error"),
    LOGIN_SUCCESS(20000, "登录成功", "ok"),
    LOGIN_FAIL(20001, "登录失败，用户名或密码错误", "error"),
    LOGGED_IN(20002, "已登录", "ok"), 
    USER_NOT_EXIST(20003, "用户不存在", "error"),
    LOGOUT_FAIL(20004, "未登录", "error"),
    LOGOUT_SUCCESS(20005, "登出成功", "ok"),
    BOOK_GET_SUCCESS(30000, "书籍查找成功", "ok"),
    BOOK_GET_FAIL(30001,"书籍不存在","error"),
    BOOK_ADD_SUCCESS(30002, "书籍添加成功", "ok"),
    BOOK_ADD_FAIL(30003, "书籍添加失败", "error"),
    BOOK_UPDATE_SUCCESS(30004, "书籍更新成功", "ok"),
    BOOK_UPDATE_FAIL(30005, "书籍更新失败", "error"),
    BOOK_DELETE_SUCCESS(30006, "书籍删除成功", "ok"),
    BOOK_DELETE_FAIL(30007, "书籍删除失败", "error"),
    
    // 购物车相关状态码
    CART_ADD_SUCCESS(40000, "添加购物车成功", "ok"),
    CART_ADD_FAIL(40001, "添加购物车失败", "error"),
    CART_UPDATE_SUCCESS(40002, "更新购物车成功", "ok"),
    CART_UPDATE_FAIL(40003, "更新购物车失败", "error"),
    CART_DELETE_SUCCESS(40004, "删除购物车项成功", "ok"),
    CART_DELETE_FAIL(40005, "删除购物车项失败", "error"),
    CART_CLEAR_SUCCESS(40006, "清空购物车成功", "ok"),
    CART_CLEAR_FAIL(40007, "清空购物车失败", "error"),
    
    // 订单相关状态码
    ORDER_CREATE_SUCCESS(50000, "创建订单成功", "ok"),
    ORDER_CREATE_FAIL(50001, "创建订单失败", "error"),
    ORDER_PAY_SUCCESS(50002, "支付订单成功", "ok"),
    ORDER_PAY_FAIL(50003, "支付订单失败", "error"),
    ORDER_CANCEL_SUCCESS(50004, "取消订单成功", "ok"),
    ORDER_CANCEL_FAIL(50005, "取消订单失败", "error"),
    ORDER_DELETE_SUCCESS(50006, "删除订单成功", "ok"),
    ORDER_DELETE_FAIL(50007, "删除订单失败", "error"),
    ORDER_NOT_EXIST(50008, "订单不存在", "error"),
    ORDER_STATUS_ERROR(50009, "订单状态错误", "error"),
    ORDER_PERMISSION_DENIED(50010, "无权操作此订单", "error"),
    
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
