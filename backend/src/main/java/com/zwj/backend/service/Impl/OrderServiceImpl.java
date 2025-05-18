package com.zwj.backend.service.Impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
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
import java.util.stream.Collectors;

import static com.zwj.backend.entity.table.OrderTableDef.ORDER;
import static com.zwj.backend.entity.table.OrderItemTableDef.ORDER_ITEM;
import static com.zwj.backend.entity.table.OrderAddressTableDef.ORDER_ADDRESS;

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
    
    @Autowired
    private OrderAddressMapper orderAddressMapper;
    
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
            
            // 加载订单地址信息
            OrderAddress address = QueryChain.of(orderAddressMapper)
                    .where(ORDER_ADDRESS.ORDER_ID.eq(order.getId()))
                    .one();
            order.setAddress(address);
        }

        return Result.success(orders);
    }

    /**
     * 创建订单
     * @param request 订单请求对象，包含商品ID和数量
     * @return 创建的订单对象
     */
    @Override
    @Transactional
    public Result<Order> addOrder(OrderRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }

        // 检查请求是否有效
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Result.error(400, "订单项不能为空");
        }

        // 检查地址信息
        if (request.getAddressInfo() == null) {
            return Result.error(400, "收货地址不能为空");
        }

        // 计算总价并检查库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderRequest.OrderItemRequest> orderItems = request.getItems();
        
        for (OrderRequest.OrderItemRequest item : orderItems) {
            Book book = bookMapper.selectOneById(item.getBookId());
            if (book == null) {
                return Result.error(404, "图书不存在: " + item.getBookId());
            }
            if (book.getStock() < item.getQuantity()) {
                return Result.error(400, "图书库存不足: " + book.getTitle());
            }
            totalAmount = totalAmount.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 创建订单
        Order order = new Order();
        order.setUserId(currentUser.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING"); // 待付款
        order.setRemark(request.getRemark()); // 设置订单备注
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setPaymentMethod(request.getPaymentMethod()); // 设置支付方式

        orderMapper.insertOrder(order);

        // 创建订单项
        List<OrderItem> orderItemsList = new ArrayList<>();
        List<Long> bookIds = new ArrayList<>(); // 用于记录处理的图书ID，用于后续清空购物车

        for (OrderRequest.OrderItemRequest item : orderItems) {
            Book book = bookMapper.selectOneById(item.getBookId());
            bookIds.add(item.getBookId()); // 记录图书ID

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setBookId(item.getBookId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(book.getPrice());
            orderItem.setCreateTime(LocalDateTime.now());
            orderItem.setUpdateTime(LocalDateTime.now());
            orderItemMapper.insertOrderItem(orderItem);

            // 减少库存
            bookService.updateStock(book.getId(), item.getQuantity());

            orderItem.setBook(book);
            orderItemsList.add(orderItem);
        }

        // 清空购物车中已下单的商品
        if (!bookIds.isEmpty()) {
            try {
                // 从购物车中删除这些图书
                cartItemMapper.deleteByQuery(
                    QueryWrapper.create()
                        .where("user_id = ?", currentUser.getId())
                        .and("book_id in (" + String.join(",", bookIds.stream().map(String::valueOf).collect(Collectors.toList())) + ")")
                );
            } catch (Exception e) {
                System.err.println("清空购物车失败: " + e.getMessage());
                // 不影响主流程，即使清空购物车失败，订单创建仍然成功
            }
        }

        order.setOrderItems(orderItemsList);
        
        // 保存地址信息
        OrderAddress address = new OrderAddress();
        address.setOrderId(order.getId());
        address.setReceiver(request.getAddressInfo().getReceiver());
        address.setPhone(request.getAddressInfo().getPhone());
        
        // 处理地区信息
        List<String> region = request.getAddressInfo().getRegion();
        if (region != null && region.size() >= 3) {
            // 将地区代码转换为实际地区名称
            String provinceCode = region.get(0);
            String cityCode = region.get(1);
            String districtCode = region.get(2);
            
            address.setProvince(getRegionNameByCode(provinceCode));
            address.setCity(getRegionNameByCode(cityCode));
            address.setDistrict(getRegionNameByCode(districtCode));
        }
        
        address.setDetailAddress(request.getAddressInfo().getDetailAddress());
        address.setZipCode(request.getAddressInfo().getZipCode());
        address.setIsDefault(request.getAddressInfo().getIsDefault());
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        
        orderAddressMapper.insert(address);
        
        // 设置订单地址
        order.setAddress(address);
        
        // 设置用户信息
        setUser(order, currentUser);

        System.out.println(order);
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

        // 删除订单地址
        orderAddressMapper.deleteByQuery(
                QueryWrapper.create().where(ORDER_ADDRESS.ORDER_ID.eq(id))
        );

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

        // 加载订单地址信息
        OrderAddress address = QueryChain.of(orderAddressMapper)
                .where(ORDER_ADDRESS.ORDER_ID.eq(order.getId()))
                .one();
        order.setAddress(address);

        //加载用户信息
        currentUser.setPassword("");
        order.setUser(currentUser);

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

        order.setUser(user);
    }

    // 实现地区代码到名称的转换方法
    private String getRegionNameByCode(String code) {
        // 这里是一个简化的实现，真实项目中应该从数据库或配置文件获取
        // 北京市
        if ("110000".equals(code)) return "北京市";
        if ("110100".equals(code)) return "北京市";
        if ("110101".equals(code)) return "东城区";
        if ("110102".equals(code)) return "西城区";
        if ("110105".equals(code)) return "朝阳区";
        if ("110106".equals(code)) return "丰台区";
        
        // 上海市
        if ("310000".equals(code)) return "上海市";
        if ("310100".equals(code)) return "上海市";
        if ("310101".equals(code)) return "黄浦区";
        if ("310104".equals(code)) return "徐汇区";
        if ("310112".equals(code)) return "闵行区";
        if ("310114".equals(code)) return "嘉定区";
        
        // 如果找不到对应的名称，则返回原始代码
        return code;
    }
} 