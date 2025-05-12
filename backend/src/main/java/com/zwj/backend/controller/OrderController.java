package com.zwj.backend.controller;

import com.mybatisflex.core.paginate.Page;
import com.zwj.backend.entity.Order;
import com.zwj.backend.entity.Result;
import com.zwj.backend.entity.dto.OrderRequest;
import com.zwj.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<List<Order>> getOrders() {
        return orderService.getOrders();
    }

    @PostMapping
    public Result<Order> createOrder(@RequestBody OrderRequest request) {
        return orderService.addOrder(request);
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable("id") Integer id) {
        return orderService.getOrderDetail(id);
    }

    @DeleteMapping("/{id}")
    public Result<Order> deleteOrder(@PathVariable("id") Integer id) {
        return orderService.deleteOrder(id);
    }

    @GetMapping("/search")
    public Result<Page<Order>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return orderService.searchOrders(keyword, start, end, page, pageSize);
    }

    @PostMapping("/{id}/pay")
    public Result<Order> payOrder(
            @PathVariable("id") Integer id,
            @RequestParam String paymentMethod) {
        return orderService.payOrder(id, paymentMethod);
    }

    @PostMapping("/{id}/cancel")
    public Result<Order> cancelOrder(@PathVariable("id") Integer id) {
        return orderService.cancelOrder(id);
    }

    // 管理员接口
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Order>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Order>> searchAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return orderService.searchAllOrders(keyword, page, pageSize, start, end);
    }
}