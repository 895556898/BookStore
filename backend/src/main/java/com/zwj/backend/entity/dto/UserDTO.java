package com.zwj.backend.entity.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String phone;

    public UserDTO(String username, String password, String phone) {
        this.username = username;
        this.password = password;
        this.phone = phone;
    }

    public UserDTO() {

    }
}
