package com.yezishuo.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private Integer id;
    private String username;
    private String realName;
    private String role;
    private Integer roleLevel;
    private Integer status;
}