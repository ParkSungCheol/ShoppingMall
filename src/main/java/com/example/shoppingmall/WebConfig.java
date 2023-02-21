package com.example.shoppingmall;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://ec2-43-200-181-206.ap-northeast-2.compute.amazonaws.com:8080", "http://localhost:8080", "https://www.juromarket.kro.kr")
                .allowedMethods("GET", "POST")
                .allowCredentials(true);
    }
}