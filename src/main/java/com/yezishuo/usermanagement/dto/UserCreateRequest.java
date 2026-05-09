package com.yezishuo.usermanagement.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String realName;
    private Integer roleLevel;  // 0=超级管理员, 1=普通用户, 2=游客
    private String shopName;
}