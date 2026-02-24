package xyz.yaungyue.secondhand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源
        config.addAllowedOriginPattern("*"); // 允许所有域名访问
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有HTTP方法
        config.setAllowCredentials(true); // 允许携带认证信息
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径生效
        
        return new CorsFilter(source);
    }
}