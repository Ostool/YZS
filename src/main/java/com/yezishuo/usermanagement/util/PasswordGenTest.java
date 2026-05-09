package com.yezishuo.usermanagement.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "123456";
        String encodedPassword = encoder.encode(password);

        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);

        // 验证加密是否正确
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证: " + matches);
    }
}
