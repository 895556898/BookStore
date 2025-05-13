package com.zwj.backend;

import com.zwj.backend.entity.Order;
import com.zwj.backend.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    OrderMapper orderMapper;

    @Test
    void contextLoads() {
        Order order = new Order();
        order.setUserId(2L);
        order.setTotalAmount(BigDecimal.valueOf(60));
        order.setStatus("PENDING"); // 待付款
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insertOrder(order);
        System.out.println(order);
    }

}
