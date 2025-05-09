package com.zwj.backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zwj.backend.mapper")
public class MyBatisPlusConfig {
    // MyBatis-Flex相关配置
}