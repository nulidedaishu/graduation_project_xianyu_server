package xyz.yaungyue.secondhand.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.yaungyue.secondhand.model.entity.Admin;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.MenuService;
import xyz.yaungyue.secondhand.service.RoleService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 配置类
 *
 * 职责分离设计：
 * 1. Spring Security 已在 SecurityConfig 中完成身份认证（是否登录）
 * 2. Sa-Token 专注于权限授权（是否有权执行操作）
 *
 * 本配置类：
 * - 实现 StpInterface 接口，提供角色和权限数据
 * - 注册 Sa-Token 拦截器（可选，用于全局权限校验）
 * - 支持在 Controller/Service 层使用 @SaCheckRole、@SaCheckPermission 注解
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final RoleService roleService;
    private final MenuService menuService;

    /**
     * 注册 Sa-Token 拦截器
     * 注意：由于Spring Security已经处理了登录认证，这里不需要再配置登录校验拦截器
     * 如果需要全局权限校验，可以取消下面的注释
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 方案1：全局权限拦截（如果需要）
        // 注意：这里不配置 StpUtil.checkLogin()，因为登录认证已由Spring Security处理
        // 可以配置全局的权限校验，如必须具有某个角色才能访问某些路径

        // registry.addInterceptor(new SaInterceptor(handle -> {
        //     // 可以在这里添加全局权限校验逻辑
        //     // 例如：某些路径需要特定角色
        // })).addPathPatterns("/api/admin/**")
        //   .excludePathPatterns("/api/auth/**");

        log.info("Sa-Token 拦截器配置完成 - 权限授权功能已启用");
    }

    /**
     * Sa-Token 权限认证接口扩展
     * 实现此接口，告诉框架如何获取用户的角色和权限
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {

            /**
             * 返回一个账号所拥有的权限码集合
             * @param loginId 账号id（即用户id）
             * @param loginType 账号类型（user/admin）
             * @return 权限码集合
             */
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                List<String> permissionList = new ArrayList<>();

                try {
                    // 根据登录类型获取权限
                    if ("admin".equals(loginType)) {
                        // 管理员权限：从菜单表中获取
                        Long adminId = Long.valueOf(loginId.toString());
                        permissionList = menuService.getPermissionsByAdminId(adminId);
                        log.debug("获取管理员权限列表, adminId: {}, permissions: {}", adminId, permissionList);
                    } else {
                        // 普通用户权限：从菜单表中获取
                        Long userId = Long.valueOf(loginId.toString());
                        permissionList = menuService.getPermissionsByUserId(userId);
                        log.debug("获取用户权限列表, userId: {}, permissions: {}", userId, permissionList);
                    }
                } catch (Exception e) {
                    log.error("获取权限列表失败, loginId: {}, loginType: {}", loginId, loginType, e);
                }

                return permissionList;
            }

            /**
             * 返回一个账号所拥有的角色标识集合
             * @param loginId 账号id（即用户id）
             * @param loginType 账号类型（user/admin）
             * @return 角色标识集合
             */
            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                List<String> roleList = new ArrayList<>();

                try {
                    if ("admin".equals(loginType)) {
                        // 获取管理员角色
                        Long adminId = Long.valueOf(loginId.toString());
                        roleList = roleService.getRolesByAdminId(adminId);
                        log.debug("获取管理员角色列表, adminId: {}, roles: {}", adminId, roleList);
                    } else {
                        // 获取用户角色
                        Long userId = Long.valueOf(loginId.toString());
                        roleList = roleService.getRolesByUserId(userId);
                        log.debug("获取用户角色列表, userId: {}, roles: {}", userId, roleList);
                    }
                } catch (Exception e) {
                    log.error("获取角色列表失败, loginId: {}, loginType: {}", loginId, loginType, e);
                }

                return roleList;
            }
        };
    }
}
