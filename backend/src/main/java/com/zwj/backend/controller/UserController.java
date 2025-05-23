package com.zwj.backend.controller;

import com.zwj.backend.entity.Result;
import com.zwj.backend.entity.User;
import com.zwj.backend.entity.dto.RegisterRequest;
import com.zwj.backend.entity.dto.UserSessionResponse;
import com.zwj.backend.service.Impl.UserService;
import com.zwj.backend.service.Impl.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(originPatterns = "*", allowCredentials = "true", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private SessionService sessionService;

    @Getter
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    //注册
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        return userService.register(request.getUsername(), request.getPassword(), request.getPhone());
    }

    //检查是否登录
    @GetMapping("/check")
    public Result<Void> check(){
        try {
            UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(20006, "未登录");
        }
    }
    
    //通过用户名检查会话状态
    @GetMapping("/checkSession")
    public Result<UserSessionResponse> checkSessionByUsername(@RequestParam String username, HttpServletRequest request) {
        // 检查用户是否存在
        User user = userService.getUserEntityByUsername(username);
        if (user == null) {
            return Result.error(20003, "用户不存在");
        }
        
        // 检查该用户是否有有效会话
        boolean isLoggedIn = sessionService.isUserLoggedIn(username);
        if (!isLoggedIn) {
            return Result.error(20006, "未登录");
        }
        
        // 获取用户的会话ID
        String sessionId = sessionService.getSessionId(username);
        HttpSession currentSession = request.getSession(false);
        
        // 创建响应对象
        UserSessionResponse response = new UserSessionResponse();
        response.setSessionValid(isLoggedIn);
        
        // 如果当前请求的会话ID与存储的用户会话ID匹配，则直接使用
        if (currentSession != null && currentSession.getId().equals(sessionId)) {
            response.setCurrentSession(true);
            
            // 返回用户信息但不包含敏感数据
            User userInfo = new User();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setPhone(user.getPhone());
            userInfo.setRole(user.getRole());
            
            response.setUser(userInfo);
        } else {
            response.setCurrentSession(false);
        }
        
        return Result.success(response);
    }
}
