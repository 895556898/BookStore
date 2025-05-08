package com.zwj.backend.controller;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.service.UserService;
import com.zwj.backend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Scope("session")
public class LoginController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public LoginController(UserService userService, PasswordEncoder passwordEncoder, SessionService sessionService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public StatusCode login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) throws Exception {
        // 检查用户是否已经登录
        if (sessionService.isUserLoggedIn(username)) {
            // 获取之前的sessionId
            String oldSessionId = sessionService.getSessionId(username);
            // 使之前的session失效
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null && oldSession.getId().equals(oldSessionId)) {
                oldSession.invalidate();
            }
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userService);
        Authentication authenticate = daoAuthenticationProvider.authenticate(usernamePasswordAuthenticationToken);
        
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        HttpSession session = request.getSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        
        // 保存新的session信息
        sessionService.addSession(username, session.getId());
        
        SecurityContextHolder.clearContext();
        return userService.getUserByUsername(username);
    }

    @PostMapping("/logout")
    public StatusCode logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute("SPRING_SECURITY_CONTEXT") == null) {
            return StatusCode.LOGOUT_FAIL;
        }
        
        // 从session中获取用户名
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            // 移除session记录
            sessionService.removeSession(username);
        }
        
        session.invalidate();
        return StatusCode.LOGOUT_SUCCESS;
    }
}
