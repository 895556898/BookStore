package com.zwj.backend.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    // 使用ConcurrentHashMap来存储用户名和sessionId的映射
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    public void addSession(String username, String sessionId) {
        userSessionMap.put(username, sessionId);
    }

    public String getSessionId(String username) {
        return userSessionMap.get(username);
    }

    public void removeSession(String username) {
        userSessionMap.remove(username);
    }

    public boolean isUserLoggedIn(String username) {
        return userSessionMap.containsKey(username);
    }
} 