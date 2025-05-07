package com.zwj.backend.controller;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.dto.RegisterRequest;
import com.zwj.backend.service.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Getter
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    //注册
    @PostMapping("register")
    public StatusCode register(@RequestBody RegisterRequest request) {
        return userService.register(request.getUsername(), request.getPassword(), request.getPhone());
    }

    //检查是否登录
    @GetMapping("/check")
    public StatusCode check(){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return StatusCode.LOGGED_IN;
    }
}
