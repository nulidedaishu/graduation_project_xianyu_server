package xyz.yaungyue.secondhand.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 注册各种拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求日志拦截器
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(     // 排除不需要记录日志的路径
                        "/static/**",
                        "/webjars/**", 
                        "/favicon.ico",
                        "/error",
                        "/actuator/**"
                );
    }
}