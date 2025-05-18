package com.zwj.backend.service;

import com.mybatisflex.core.paginate.Page;
import com.zwj.backend.entity.CartItem;
import com.zwj.backend.entity.Order;
import com.zwj.backend.entity.Result;
import com.zwj.backend.entity.dto.OrderRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    // 获取当前用户的订单列表
    Result<List<Order>> getOrders();
    
    // 创建订单
    Result<Order> addOrder(OrderRequest request);
    
    // 在购物车项列表中查找指定ID的购物车项
    CartItem findCartItemById(List<CartItem> cartItems, Long cartItemId);
    
    // 删除订单
    Result<Order> deleteOrder(Long id);
    
//    // 获取所有订单（管理员）
//    Result<List<Order>> getAllOrders();
    
    // 搜索所有订单（管理员）
    Result<Page<Order>> searchAllOrders(String keyword, int page, int pageSize, LocalDateTime start, LocalDateTime end);
    
    // 搜索当前用户的订单
    Result<Page<Order>> searchOrders(String keyword, LocalDateTime start, LocalDateTime end, int page, int pageSize);
    
    // 获取订单详情
    Result<Order> getOrderDetail(Long id);
    
    // 支付订单
    Result<Order> payOrder(Long id, String paymentMethod);
    
    // 取消订单
    Result<Order> cancelOrder(Long id);

    //完成订单
    Result<Order> completeOrder(Long id);
} 