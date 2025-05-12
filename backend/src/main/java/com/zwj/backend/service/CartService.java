package com.zwj.backend.service;

import com.zwj.backend.entity.CartItem;
import com.zwj.backend.entity.Result;

import java.util.List;

public interface CartService {
    // 获取当前用户的购物车
    Result<List<CartItem>> getCart();
    
    // 添加商品到购物车
    Result<CartItem> addToCart(Integer bookId, Integer quantity);
    
    // 更新购物车项数量
    Result<CartItem> updateCartItem(Integer cartItemId, Integer quantity);
    
    // 删除购物车项
    Result<Boolean> removeFromCart(Integer cartItemId);
    
    // 清空购物车
    Result<Boolean> clearCart();
    
    // 获取购物车总价
    Result<Double> getCartTotal();
    
    // 通过ID查找购物车项
    CartItem findCartItemById(Integer cartItemId);
    
    // 通过ID列表获取购物车项列表
    List<CartItem> getCartItemsByIds(List<Integer> cartItemIds);
} 