package com.zwj.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
} 