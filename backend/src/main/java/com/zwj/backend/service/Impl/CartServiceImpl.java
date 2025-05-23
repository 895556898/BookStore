package com.zwj.backend.service.Impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.zwj.backend.entity.*;
import com.zwj.backend.mapper.*;
import com.zwj.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.zwj.backend.entity.table.CartItemTableDef.CART_ITEM;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookTagMapper bookTagMapper;

    @Autowired
    private TagMapper tagMapper;

    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
        }
        return null;
    }

    //获取购物车列表
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
            book.setTag(getTagsByBookId(book.getId()));
            cartItem.setBook(book);
        }

        return Result.success(cartItems);
    }

    //添加商品到购物车
    @Override
    @Transactional
    public Result<CartItem> addToCart(Long bookId, Integer quantity) {
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
            cartItem.setUserId(currentUser.getId());
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
    public Result<CartItem> updateCartItem(Long cartItemId, Integer quantity) {
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

    //删除购物车项
    @Override
    @Transactional
    public Result<Boolean> removeFromCart(Long cartItemId) {
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

    //获取购物车总价
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

    // 加载图书tag
    public List<Tag> getTagsByBookId(Long bookId) {
        List<BookTag> bookTags = bookTagMapper.selectListByQuery(
                QueryWrapper.create().where("bid = ?", bookId));
        if (bookTags == null || bookTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = bookTags.stream().map(BookTag::getTid).collect(Collectors.toList());

        QueryWrapper query = QueryWrapper.create();
        if (!tagIds.isEmpty()) {
            query.where(Tag::getId).in(tagIds);
        }

        return tagMapper.selectListByQuery(query);
    }
} 