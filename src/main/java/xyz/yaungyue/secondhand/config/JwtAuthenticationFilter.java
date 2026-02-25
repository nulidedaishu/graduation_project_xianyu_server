package xyz.yaungyue.secondhand.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
                // 检查Sa-Token是否有效登录
                if (StpUtil.isLogin()) {
                    Long userId = StpUtil.getLoginIdAsLong();

                    // 将用户ID存入request属性，供后续使用
                    request.setAttribute("currentUserId", userId);
                    request.setAttribute("currentLoginType", StpUtil.getLoginType());

                    log.debug("JWT认证通过，用户ID: {}, 登录类型: {}", userId, StpUtil.getLoginType());
                }
            } catch (Exception e) {
                log.warn("Token解析失败: {}", e.getMessage());
                // 继续执行，让SecurityConfig的authenticated()规则处理未认证情况
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
               uri.startsWith("/swagger-ui/") ||          // Swagger UI
               uri.startsWith("/v3/api-docs/") ||         // API文档
               uri.startsWith("/static/") ||              // 静态资源
               uri.equals("/favicon.ico");                // 网站图标
    }
}
