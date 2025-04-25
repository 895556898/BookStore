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
