package com.yezishuo.usermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String userDir = System.getProperty("user.dir");
        String uploadPath = "file:" + userDir + "/uploads/pictures/";

        // 映射 /uploads/pictures/** 到物理路径
        registry.addResourceHandler("/uploads/pictures/**")
                .addResourceLocations(uploadPath);

        // 打印路径，方便调试
        System.out.println("图片上传目录: " + uploadPath);
    }
}