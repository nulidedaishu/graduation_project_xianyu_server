package xyz.yaungyue.secondhand.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 配置类
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        // 临时禁用 SaToken 拦截器以进行测试
        /*
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 根据路由划分校验等级
        })).addPathPatterns("/**")
          .excludePathPatterns(
              "/api/auth/login",
              "/api/auth/logout",
              "/api/products",
              "/api/categories",
              "/swagger-ui/**",
              "/v3/api-docs/**",
              "/static/**",
              "/index.html",
              "/login-test.html"
          );
        */
    }

    /**
     * Sa-Token 权限认证接口实现
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            /**
             * 返回一个账号所拥有的权限码集合
             */
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                // TODO: 实现权限获取逻辑
                return new ArrayList<>();
            }

            /**
             * 返回一个账号所拥有的角色标识集合
             */
            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                // TODO: 实现角色获取逻辑
                return new ArrayList<>();
            }
        };
    }
}