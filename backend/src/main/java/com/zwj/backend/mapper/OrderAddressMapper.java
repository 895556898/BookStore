package com.zwj.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.OrderAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderAddressMapper extends BaseMapper<OrderAddress> {
    // 可以添加自定义方法
} 