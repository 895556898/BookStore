package com.zwj.backend.controller;

import com.zwj.backend.entity.CartItem;
import com.zwj.backend.entity.Result;
import com.zwj.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<List<CartItem>> getCart() {
        return cartService.getCart();
    }

    @PostMapping("/add")
    public Result<CartItem> addToCart(
            @RequestParam Integer bookId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return cartService.addToCart(bookId, quantity);
    }

    @PutMapping("/update/{id}")
    public Result<CartItem> updateCartItem(
            @PathVariable("id") Integer cartItemId,
            @RequestParam Integer quantity) {
        return cartService.updateCartItem(cartItemId, quantity);
    }

    @DeleteMapping("/remove/{id}")
    public Result<Boolean> removeFromCart(@PathVariable("id") Integer cartItemId) {
        return cartService.removeFromCart(cartItemId);
    }

    @DeleteMapping("/clear")
    public Result<Boolean> clearCart() {
        return cartService.clearCart();
    }

    @GetMapping("/total")
    public Result<Double> getCartTotal() {
        return cartService.getCartTotal();
    }
} 