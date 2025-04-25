package com.zwj.backend.controller;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.dto.UserDTO;
import com.zwj.backend.entity.vo.LoginResultVO;
import com.zwj.backend.service.UserService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("register")
    public ResponseEntity<LoginResultVO> register(@RequestBody UserDTO dto) {
        StatusCode code = userService.register(dto.getUsername(), dto.getPassword(), dto.getPhone());
        if(code == StatusCode.REGISTER_SUCCESS){
            return (new LoginResultVO(code.getMsg(), "ok", sessionId));
        } else {
            return (new LoginResultVO(code.getMsg(), "error", sessionId));
        }
    }



}

//@PostMapping("/register")
//public ResponseEntity<ApiResponse<LoginResultVO>> register(
//        @Valid @RequestBody UserDTO dto,
//        BindingResult bindingResult, // 参数验证结果
//        HttpServletResponse response) {
//
//    // 参数校验
//    if (bindingResult.hasErrors()) {
//        String errorMsg = bindingResult.getFieldErrors()
//                .stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.joining(", "));
//        return ResponseEntity.badRequest()
//                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), errorMsg));
//    }
//
//    try {
//        // 注册逻辑
//        User user = userService.register(dto);
//
//        // 生成JWT令牌（替代SessionID）
//        String token = jwtTokenProvider.generateToken(user.getUsername());
//
//        // 设置响应头和安全Cookie
//        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//        ResponseCookie cookie = ResponseCookie.from("token", token)
//                .httpOnly(true)
//                .secure(true)
//                .path("/")
//                .maxAge(Duration.ofDays(7))
//                .sameSite("Strict")
//                .build();
//        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//
//        // 构建响应
//        LoginResultVO result = new LoginResultVO(
//                "ok",
//                "注册成功",
//                token);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success(result));
//
//    } catch (DuplicateUserException e) {
//        return ResponseEntity.status(HttpStatus.CONFLICT)
//                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "用户名已存在"));
//    } catch (Exception e) {
//        log.error("注册失败: {}", e.getMessage());
//        return ResponseEntity.internalServerError()
//                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统错误"));
//    }
//}