package com.zwj.backend.controller;

import com.zwj.backend.entity.CartItem;
import com.zwj.backend.entity.Result;
import com.zwj.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Result<List<CartItem>> getCart() {
        return cartService.getCart();
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public Result<CartItem> addToCart(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return cartService.addToCart(bookId, quantity);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('USER')")
    public Result<CartItem> updateCartItem(
            @PathVariable("id") Long cartItemId,
            @RequestParam Integer quantity) {
        return cartService.updateCartItem(cartItemId, quantity);
    }

    @DeleteMapping("/remove/{id}")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> removeFromCart(@PathVariable("id") Long cartItemId) {
        return cartService.removeFromCart(cartItemId);
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    public Result<Boolean> clearCart() {
        return cartService.clearCart();
    }

    @GetMapping("/total")
    @PreAuthorize("hasRole('USER')")
    public Result<Double> getCartTotal() {
        return cartService.getCartTotal();
    }
} 