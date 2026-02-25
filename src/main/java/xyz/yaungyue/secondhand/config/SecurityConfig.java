package xyz.yaungyue.secondhand.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
  * 职责分离设计：
 * 1. Spring Security 负责身份认证（Authentication）：验证用户是否已登录
 *    - 通过 JwtAuthenticationFilter 验证 Sa-Token 的有效性
 *    - 放行公共接口，保护需要登录的接口
 * 2. Sa-Token 负责权限授权（Authorization）：验证用户是否有权执行特定操作
 *    - 使用 @SaCheckRole、@SaCheckPermission 等注解进行细粒度权限控制
 *    - 在 Controller 层或 Service 层使用 Sa-Token 提供的 API 进行权限校验
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离项目通常禁用）
            .csrf(AbstractHttpConfigurer::disable)

            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置会话管理：不使用Session，因为使用Sa-Token进行状态管理
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置请求授权规则
            .authorizeHttpRequests(authz -> authz
                // ==================== 公共接口（无需登录）====================
                // 认证相关接口
                .requestMatchers("/api/auth/**").permitAll()

                // 商品浏览接口（允许匿名访问）
                .requestMatchers("/api/products").permitAll()
                .requestMatchers("/api/products/search").permitAll()
                .requestMatchers("/api/products/{id}").permitAll()
                .requestMatchers("/api/products/category/{categoryId}").permitAll()

                // 商品分类接口
                .requestMatchers("/api/categories").permitAll()
                .requestMatchers("/api/categories/{id}").permitAll()

                // Swagger UI 和 API 文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // 静态资源
                .requestMatchers("/static/**", "/favicon.ico").permitAll()

                // ==================== 需要登录的接口 ====================
                // 所有其他接口都需要认证（是否登录由Spring Security判断）
                // 具体权限（如是否是管理员）由Sa-Token的注解控制
                .anyRequest().authenticated()
            )

            // 禁用默认的HTTP Basic认证
            .httpBasic(AbstractHttpConfigurer::disable)

            // 禁用默认的表单登录
            .formLogin(AbstractHttpConfigurer::disable)

            // 添加JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
