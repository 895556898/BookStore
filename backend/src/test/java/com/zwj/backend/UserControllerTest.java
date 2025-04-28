package com.zwj.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwj.backend.controller.UserController;
import com.zwj.backend.entity.dto.UserDTO;
import com.zwj.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class) // 替换为你的Controller类
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // 用于JSON序列化

    @MockBean
    private UserService userService;

    @Test
    void testRegisterSuccess() throws Exception {
        // 准备测试数据
        UserDTO testUser = new UserDTO();
        testUser.setUsername("newUser");
        testUser.setPassword("password123");
        testUser.setPhone("13812345678");

        // 模拟Service层返回成功
        when(userService.register(
            "newUser", 
            "password123", 
            "13812345678"
        )).thenReturn(StatusCode.REGISTER_SUCCESS);

        // 执行和验证
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.message").value(StatusCode.REGISTER_SUCCESS.getMsg()))
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(request().sessionAttributeExists("SESSION_KEY"))
                .andExpect(request().sessionAttribute("username", "newUser"));
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        UserDTO existingUser = new UserDTO();
        existingUser.setUsername("existingUser");
        existingUser.setPassword("password");
        existingUser.setPhone("13800000000");

        // 模拟Service层返回用户名已存在
        when(userService.register(
            anyString(), 
            anyString(), 
            anyString()
        )).thenReturn(StatusCode.USERNAME_ALREADY_EXISTS);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(StatusCode.USERNAME_ALREADY_EXISTS.getMsg()));
    }
}