package com.zwj.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
} 