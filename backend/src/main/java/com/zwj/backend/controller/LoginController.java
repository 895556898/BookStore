package com.zwj.backend.controller;

import com.zwj.backend.common.StatusCode;
import com.zwj.backend.service.UserService;
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

    public LoginController (UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public StatusCode login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) throws Exception {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userService);
        Authentication authenticate = daoAuthenticationProvider.authenticate(usernamePasswordAuthenticationToken);//默认的authenticate
        SecurityContextHolder.getContext().setAuthentication(authenticate);//设置上下文 也就是把用户session放入上下文
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());//设置session
        SecurityContextHolder.clearContext();//清除上下文
        return userService.getUserByUsername(username);
    }

    @PostMapping("/logout")
    public StatusCode logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        if(session.getAttribute("SPRING_SECURITY_CONTEXT") == null) return StatusCode.LOGOUT_FAIL;
        session.invalidate();
        return StatusCode.LOGOUT_SUCCESS;
    }
}
