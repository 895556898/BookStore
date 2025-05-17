package com.zwj.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Insert("INSERT INTO orders (user_id, total_amount, status, payment_time, create_time, update_time, remark, payment_method) " +
            "VALUES (#{userId}, #{totalAmount}, #{status}, #{paymentTime}, #{createTime}, #{updateTime}, #{remark}, #{paymentMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertOrder(Order order);
} 