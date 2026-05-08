package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsermanagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsermanagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("系统启动成功！访问地址：");
        System.out.println("http://localhost:8080/index.html");
        System.out.println("========================================");
    }
}
