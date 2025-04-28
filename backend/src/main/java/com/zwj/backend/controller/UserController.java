package com.zwj.backend.controller;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.dto.UserDTO;
import com.zwj.backend.entity.vo.LoginResultVO;
import com.zwj.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("register")
    public ResponseEntity<LoginResultVO> register(@RequestBody UserDTO dto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        StatusCode code = userService.register(dto.getUsername(), dto.getPassword(), dto.getPhone());
        if (code == StatusCode.REGISTER_SUCCESS) {
            String sessionId = UUID.randomUUID().toString();

            // 设置会话属性
            request.getSession().setAttribute("SESSION_KEY", sessionId);
            request.getSession().setAttribute("username", dto.getUsername());

            // 设置响应头
            response.setHeader("Authorization", "Bearer " + sessionId);

            // 返回成功响应
            return ResponseEntity.ok(new LoginResultVO(code.getMsg(), "ok", sessionId));
        } else {
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResultVO(code.getMsg(), "error", null));
        }
    }

    public  ResponseEntity<LoginResultVO> login(@RequestBody UserDTO dto, HttpServletRequest request) {

    }
}
