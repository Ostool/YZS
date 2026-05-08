package com.example.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private String username;
        private String role;
    }
}