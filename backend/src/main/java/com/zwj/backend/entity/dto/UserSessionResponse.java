package com.zwj.backend.entity.dto;

import com.zwj.backend.entity.User;
import lombok.Data;

@Data
public class UserSessionResponse {

    //会话是否有效
    private boolean sessionValid;

    //是否是当前会话
    private boolean currentSession;

    //用户信息
    private User user;
} 