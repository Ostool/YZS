package com.example.usermanagement.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role;

    @Column(name = "role_level")
    private Integer roleLevel;  // 0=超级管理员, 1=普通用户, 2=游客

    @Column(name = "real_name")
    private String realName;

    private Integer status;  // 0=禁用, 1=启用

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (roleLevel == null) roleLevel = 2;
        if (status == null) status = 1;
    }

    @Column(name = "shop_name")
    private String shopName;
}