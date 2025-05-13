package com.zwj.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.Order;
import com.zwj.backend.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    @Insert("INSERT INTO order_items (order_id, book_id, quantity, price, create_time, update_time) " +
            "VALUES (#{orderId}, #{bookId}, #{quantity}, #{price}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertOrderItem(OrderItem orderItem);
} 