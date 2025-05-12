package com.zwj.backend.service.Impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.zwj.backend.entity.Book;
import com.zwj.backend.entity.CartItem;
import com.zwj.backend.entity.Result;
import com.zwj.backend.entity.User;
import com.zwj.backend.entity.table.CartItemTableDef;
import com.zwj.backend.mapper.BookMapper;
import com.zwj.backend.mapper.CartItemMapper;
import com.zwj.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.zwj.backend.entity.table.CartItemTableDef.CART_ITEM;
import static com.zwj.backend.entity.table.BookTableDef.BOOK;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;
    
    @Autowired
    private BookMapper bookMapper;
    
    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
    
    @Override
    public Result<List<CartItem>> getCart() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 查询购物车项
        List<CartItem> cartItems = QueryChain.of(cartItemMapper)
                .where(CART_ITEM.USER_ID.eq(currentUser.getId()))
                .list();
        
        // 加载购物车项中的图书信息
        for (CartItem cartItem : cartItems) {
            Book book = bookMapper.selectOneById(cartItem.getBookId());
            cartItem.setBook(book);
        }
        
        return Result.success(cartItems);
    }

    @Override
    @Transactional
    public Result<CartItem> addToCart(Integer bookId, Integer quantity) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 检查图书是否存在
        Book book = bookMapper.selectOneById(bookId);
        if (book == null) {
            return Result.error(404, "图书不存在");
        }
        
        // 检查库存
        if (book.getStock() < quantity) {
            return Result.error(400, "库存不足");
        }
        
        // 检查购物车中是否已存在该图书
        CartItem existingItem = QueryChain.of(cartItemMapper)
                .where(CART_ITEM.USER_ID.eq(currentUser.getId()))
                .and(CART_ITEM.BOOK_ID.eq(bookId))
                .one();
        
        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.update(existingItem);
            existingItem.setBook(book);
            return Result.success(existingItem);
        } else {
            // 创建新购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUserId((int)currentUser.getId());
            cartItem.setBookId(bookId);
            cartItem.setQuantity(quantity);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(cartItem);
            cartItem.setBook(book);
            return Result.success(cartItem);
        }
    }

    @Override
    @Transactional
    public Result<CartItem> updateCartItem(Integer cartItemId, Integer quantity) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 查询购物车项
        CartItem cartItem = QueryChain.of(cartItemMapper)
                .where(CART_ITEM.ID.eq(cartItemId))
                .and(CART_ITEM.USER_ID.eq(currentUser.getId()))
                .one();
        
        if (cartItem == null) {
            return Result.error(404, "购物车项不存在");
        }
        
        // 检查图书库存
        Book book = bookMapper.selectOneById(cartItem.getBookId());
        if (book.getStock() < quantity) {
            return Result.error(400, "库存不足");
        }
        
        // 更新数量
        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemMapper.update(cartItem);
        cartItem.setBook(book);
        
        return Result.success(cartItem);
    }

    @Override
    @Transactional
    public Result<Boolean> removeFromCart(Integer cartItemId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 查询购物车项
        CartItem cartItem = QueryChain.of(cartItemMapper)
                .where(CART_ITEM.ID.eq(cartItemId))
                .and(CART_ITEM.USER_ID.eq(currentUser.getId()))
                .one();
        
        if (cartItem == null) {
            return Result.error(404, "购物车项不存在");
        }
        
        // 删除购物车项
        cartItemMapper.deleteById(cartItemId);
        
        return Result.success(true);
    }

    @Override
    @Transactional
    public Result<Boolean> clearCart() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 删除当前用户的所有购物车项
        cartItemMapper.deleteByQuery(
                QueryWrapper.create().where(CART_ITEM.USER_ID.eq(currentUser.getId()))
        );
        
        return Result.success(true);
    }

    @Override
    public Result<Double> getCartTotal() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 查询购物车项
        List<CartItem> cartItems = QueryChain.of(cartItemMapper)
                .where(CART_ITEM.USER_ID.eq(currentUser.getId()))
                .list();
        
        double total = 0.0;
        for (CartItem cartItem : cartItems) {
            Book book = bookMapper.selectOneById(cartItem.getBookId());
            if (book != null) {
                total += book.getPrice().doubleValue() * cartItem.getQuantity();
            }
        }
        
        return Result.success(total);
    }

    @Override
    public CartItem findCartItemById(Integer cartItemId) {
        return cartItemMapper.selectOneById(cartItemId);
    }

    @Override
    public List<CartItem> getCartItemsByIds(List<Integer> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return new ArrayList<>();
        }
        return QueryChain.of(cartItemMapper)
                .where(CART_ITEM.ID.in(cartItemIds))
                .list();
    }
} 