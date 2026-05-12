package com.yezishuo.usermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String userDir = System.getProperty("user.dir");

        // 映射 /uploads/** 到物理路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");

        // 巡检图片目录
        registry.addResourceHandler("/uploads/promotionpictures/**")
                .addResourceLocations("file:" + userDir + "/uploads/promotionpictures/");
    }
}