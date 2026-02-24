package xyz.yaungyue.secondhand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF保护
                .authorizeHttpRequests(authz -> authz
                        // 允许匿名访问认证相关接口
                        .requestMatchers("/api/auth/**").permitAll()
                        // 允许访问商品列表接口
                        .requestMatchers("/api/products").permitAll()
                        // 允许访问商品分类接口
                        .requestMatchers("/api/categories").permitAll()
                        // 允许访问Swagger UI相关资源（如果有的话）
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 允许访问静态资源
                        .requestMatchers("/static/**", "/favicon.ico").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable) // 禁用HTTP Basic认证
                .formLogin(AbstractHttpConfigurer::disable); // 禁用表单登录

        return http.build();
    }
}
