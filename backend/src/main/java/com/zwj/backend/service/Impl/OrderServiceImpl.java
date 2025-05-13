package com.zwj.backend.service.Impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.zwj.backend.entity.*;
import com.zwj.backend.entity.dto.OrderRequest;
import com.zwj.backend.mapper.*;
import com.zwj.backend.service.BookService;
import com.zwj.backend.service.CartService;
import com.zwj.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.zwj.backend.entity.table.OrderTableDef.ORDER;
import static com.zwj.backend.entity.table.OrderItemTableDef.ORDER_ITEM;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private CartItemMapper cartItemMapper;
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private BookService bookService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookTagMapper bookTagMapper;

    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private OrderTransactionMapper orderTransactionMapper;
    
    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
        }
        return null;
    }
    
    @Override
    public Result<List<Order>> getOrders() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        List<Order> orders = QueryChain.of(orderMapper)
                .where(ORDER.USER_ID.eq(currentUser.getId()))
                .orderBy(ORDER.CREATE_TIME.desc())
                .list();
        
        // 加载订单项
        for (Order order : orders) {
            List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                    .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                    .list();
            
            // 加载图书信息
            for (OrderItem orderItem : orderItems) {
                Book book = bookMapper.selectOneById(orderItem.getBookId());
                book.setTag(getTagsByBookId(book.getId()));
                orderItem.setBook(book);
            }
            
            order.setOrderItems(orderItems);
        }

        return Result.success(orders);
    }

    /**
     * 创建订单
     * @param request 订单请求对象，包含购物车项ID列表
     * @return 创建的订单对象
     */
    @Override
    @Transactional
    public Result<Order> addOrder(OrderRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }

        // 获取购物车项
        List<CartItem> cartItems = cartService.getCartItemsByIds(request.getCartItemIds());
        if (cartItems.isEmpty()) {
            return Result.error(400, "购物车为空");
        }

        // 检查库存
        for (CartItem cartItem : cartItems) {
            Book book = bookMapper.selectOneById(cartItem.getBookId());
            if (book == null) {
                return Result.error(404, "图书不存在: " + cartItem.getBookId());
            }
            if (book.getStock() < cartItem.getQuantity()) {
                return Result.error(400, "图书库存不足: " + book.getTitle());
            }
        }

        // 计算总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Book book = bookMapper.selectOneById(cartItem.getBookId());
            totalAmount = totalAmount.add(book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 创建订单
        Order order = new Order();
        order.setUserId(currentUser.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING"); // 待付款
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insertOrder(order);

        // 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Book book = bookMapper.selectOneById(cartItem.getBookId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setBookId(cartItem.getBookId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(book.getPrice());
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            orderItemMapper.insertOrderItem(orderItem);

            // 减少库存
            bookService.updateStock(book.getId(), cartItem.getQuantity());

            orderItem.setBook(book);
            orderItems.add(orderItem);
        }
        
        // 删除购物车项
        for (CartItem cartItem : cartItems) {
            cartItemMapper.deleteById(cartItem.getId());
        }
        
        order.setOrderItems(orderItems);
        
        // 设置用户信息
        setUser(order,currentUser);
        
        return Result.success(order);
    }

    @Override
    public CartItem findCartItemById(List<CartItem> cartItems, Long cartItemId) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId().equals(cartItemId)) {
                return cartItem;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Result<Order> deleteOrder(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }

        // 查询订单
        Order order = QueryChain.of(orderMapper)
                .where(ORDER.ID.eq(id))
                .and(ORDER.USER_ID.eq(currentUser.getId()))
                .one();

        if (order == null) {
            return Result.error(404, "订单不存在");
        }

        // 只能删除已取消的订单
        if (!"CANCELLED".equals(order.getStatus())) {
            return Result.error(400, "只能删除已取消的订单");
        }

        // 删除订单项
        orderItemMapper.deleteByQuery(
                QueryWrapper.create().where(ORDER_ITEM.ORDER_ID.eq(id))
        );

        // 删除订单
        orderMapper.deleteById(id);
        
        // 设置用户信息
        setUser(order,currentUser);
        
        return Result.success(order);
    }

    @Override
    public Result<List<Order>> getAllOrders() {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        System.out.println(currentUser);
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error(403, "权限不足");
        }

        List<Order> orders = QueryChain.of(orderMapper)
                .orderBy(ORDER.CREATE_TIME.desc())
                .list();

        // 加载订单项和用户信息
        for (Order order : orders) {
            List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                    .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                    .list();

            // 加载图书信息
            for (OrderItem orderItem : orderItems) {
                Book book = bookMapper.selectOneById(orderItem.getBookId());
                book.setTag(getTagsByBookId(book.getId()));
                orderItem.setBook(book);
            }

            order.setOrderItems(orderItems);
        }

        return Result.success(orders);
    }

    @Override
    public Result<Page<Order>> searchAllOrders(String keyword, int page, int pageSize, LocalDateTime start, LocalDateTime end) {
        // 检查是否为管理员
        User currentUser = getCurrentUser();
        System.out.println(currentUser);
        if (currentUser == null || (!"admin".equals(currentUser.getRole()))) {
            return Result.error(403, "权限不足");
        }

        QueryWrapper queryWrapper = QueryWrapper.create();

        // 添加时间范围条件
        if (start != null) {
            queryWrapper.and(ORDER.CREATE_TIME.ge(start));
        }

        if (end != null) {
            queryWrapper.and(ORDER.CREATE_TIME.le(end));
        }

        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(ORDER.ID.like("%" + keyword + "%"));
        }

        queryWrapper.orderBy(ORDER.CREATE_TIME.desc());

        // 分页查询
        Page<Order> orderPage = orderMapper.paginate(page, pageSize, queryWrapper);

        // 加载订单项
        for (Order order : orderPage.getRecords()) {
            List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                    .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                    .list();

            // 加载图书信息
            for (OrderItem orderItem : orderItems) {
                Book book = bookMapper.selectOneById(orderItem.getBookId());
                book.setTag(getTagsByBookId(book.getId()));
                orderItem.setBook(book);
            }

            order.setOrderItems(orderItems);
        }

        return Result.success(orderPage);
    }

    @Override
    public Result<Page<Order>> searchOrders(String keyword, LocalDateTime start, LocalDateTime end, int page, int pageSize) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }

        QueryWrapper queryWrapper = QueryWrapper.create();

        // 添加用户条件
        queryWrapper.and(ORDER.USER_ID.eq(currentUser.getId()));

        // 添加时间范围条件
        if (start != null) {
            queryWrapper.and(ORDER.CREATE_TIME.ge(start));
        }

        if (end != null) {
            queryWrapper.and(ORDER.CREATE_TIME.le(end));
        }

        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(ORDER.ID.like("%" + keyword + "%"));
        }

        queryWrapper.orderBy(ORDER.CREATE_TIME.desc());

        // 分页查询
        Page<Order> orderPage = orderMapper.paginate(page, pageSize, queryWrapper);

        // 加载订单项
        for (Order order : orderPage.getRecords()) {
            List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                    .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                    .list();

            // 加载图书信息
            for (OrderItem orderItem : orderItems) {
                Book book = bookMapper.selectOneById(orderItem.getBookId());
                book.setTag(getTagsByBookId(book.getId()));
                orderItem.setBook(book);
            }

            order.setOrderItems(orderItems);
        }

        return Result.success(orderPage);
    }

    @Override
    public Result<Order> getOrderDetail(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }

        // 管理员可以查看所有订单，普通用户只能查看自己的订单
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ORDER.ID.eq(id));

        if (!"ADMIN".equals(currentUser.getRole())) {
            queryWrapper.and(ORDER.USER_ID.eq(currentUser.getId()));
        }

        Order order = orderMapper.selectOneByQuery(queryWrapper);

        if (order == null) {
            return Result.error(404, "订单不存在");
        }

        // 加载订单项
        List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                .list();

        // 加载图书信息
        for (OrderItem orderItem : orderItems) {
            Book book = bookMapper.selectOneById(orderItem.getBookId());
            book.setTag(getTagsByBookId(book.getId()));
            orderItem.setBook(book);
        }

        order.setOrderItems(orderItems);
        
        return Result.success(order);
    }

    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @return 支付结果
     */
    @Override
    @Transactional
    public Result<Order> payOrder(Long id, String paymentMethod) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        
        // 查询订单
        Order order = QueryChain.of(orderMapper)
                .where(ORDER.ID.eq(id))
                .and(ORDER.USER_ID.eq(currentUser.getId()))
                .one();
        
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        
        // 检查订单状态
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error(400, "订单状态不正确");
        }
        
        // 修改订单状态
        order.setStatus("PAID");
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);
        
        // 创建交易记录
        OrderTransaction transaction = new OrderTransaction();
        transaction.setOrderId(order.getId());
        transaction.setAmount(order.getTotalAmount());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        transaction.setPaymentMethod(paymentMethod);
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());
        orderTransactionMapper.insert(transaction);
        
        // 重新加载订单项
        List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                .list();
        
        // 加载图书信息
        for (OrderItem orderItem : orderItems) {
            Book book = bookMapper.selectOneById(orderItem.getBookId());
            book.setTag(getTagsByBookId(book.getId()));
            orderItem.setBook(book);
        }
        
        order.setOrderItems(orderItems);
        
        // 设置用户信息
        setUser(order,currentUser);
        
        return Result.success(order);
    }

    @Override
    @Transactional
    public Result<Order> cancelOrder(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }

        // 查询订单
        Order order = QueryChain.of(orderMapper)
                .where(ORDER.ID.eq(id))
                .and(ORDER.USER_ID.eq(currentUser.getId()))
                .one();

        if (order == null) {
            return Result.error(404, "订单不存在");
        }

        // 只能取消待付款的订单
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error(400, "只能取消待付款的订单");
        }

        if (currentUser.getId() != order.getUserId()) {
            return Result.error(400,"用户不匹配，请刷新页面重试");
        }

        // 修改订单状态
        order.setStatus("CANCELLED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.update(order);

        // 恢复库存
        List<OrderItem> orderItems = QueryChain.of(orderItemMapper)
                .where(ORDER_ITEM.ORDER_ID.eq(order.getId()))
                .list();

        for (OrderItem orderItem : orderItems) {
            Book book = bookMapper.selectOneById(orderItem.getBookId());
            if (book != null) {
                book.setStock(book.getStock() + orderItem.getQuantity());
                bookMapper.update(book);
                orderItem.setBook(book);
            }
        }

        order.setOrderItems(orderItems);
        
        // 设置用户信息
        setUser(order,currentUser);

        return Result.success(order);
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

    public void setUser(Order order, User currentUser){
        User user = new User();
        user.setUsername(currentUser.getUsername());
        user.setId(currentUser.getId());
        user.setPhone(currentUser.getPhone());
        user.setEmail(currentUser.getEmail());

        order.setUser(user);
    }
} 