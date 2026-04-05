package xyz.yaungyue.secondhand.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 职责：验证请求是否携带有效的Sa-Token，完成身份认证（Authentication）
 * 权限校验（Authorization）由Sa-Token的注解完成
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String requestUri = request.getRequestURI();

        // 放行公共接口（这些接口在SecurityConfig中已配置permitAll，但这里做双重保险）
        if (isPublicEndpoint(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 尝试从请求头中获取token
        String token = request.getHeader("Authorization");

        // 如果token存在且Sa-Token已登录，则将用户信息存入上下文
        if (token != null && !token.isEmpty()) {
            try {
                // 检查Sa-Token是否有效登录（不区分用户类型，只检查是否登录）
                // 具体的角色和权限校验由@SaCheckRole等注解处理
                if (StpUtil.isLogin()) {
                    Long userId = StpUtil.getLoginIdAsLong();
                    String loginType = StpUtil.getLoginType();

                    // 将用户ID和登录类型存入request属性，供后续使用
                    request.setAttribute("currentUserId", userId);
                    request.setAttribute("currentLoginType", loginType);

                    // 获取用户角色列表并转换为 Spring Security 权限
                    List<String> roleList = StpUtil.getRoleList();
                    List<SimpleGrantedAuthority> authorities = roleList.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    // 创建 Spring Security 认证对象
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities);

                    // 设置认证信息到 Spring Security 上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT认证通过，用户ID: {}, 登录类型: {}, 角色: {}",
                            userId, loginType, roleList);
                }
            } catch (Exception e) {
                log.warn("Token解析失败: {}", e.getMessage());
                // 清除 Security 上下文
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否为公共接口
     */
    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/api/auth/") ||           // 认证接口
               uri.equals("/api/products") ||             // 商品列表
               uri.equals("/api/categories") ||            // 分类列表
               uri.startsWith("/swagger-ui") ||           // Swagger UI (包括 /swagger-ui.html 和 /swagger-ui/)
               uri.startsWith("/v3/api-docs") ||          // API文档
               uri.startsWith("/swagger-resources") ||    // Swagger 资源
               uri.startsWith("/webjars") ||              // Swagger Webjars
               uri.startsWith("/static/") ||              // 静态资源
               uri.equals("/favicon.ico");                // 网站图标
    }
}
