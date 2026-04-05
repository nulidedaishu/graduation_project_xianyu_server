package xyz.yaungyue.secondhand.util;

import cn.dev33.satoken.stp.StpUtil;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.model.entity.Admin;

import java.util.List;

/**
 * Sa-Token 工具类
 */
public class SaTokenUtil {

    private static final String USER_TYPE = "user";
    private static final String ADMIN_TYPE = "admin";

    /**
     * 用户登录
     * @param user 用户信息
     * @return token值
     */
    public static String login(User user) {
        return login(user, false);
    }

    /**
     * 登录方法（支持区分用户类型）
     * @param user 用户信息
     * @param isAdmin 是否为管理员
     * @return token值
     */
    public static String login(Object user, boolean isAdmin) {
        Long userId;
        String userType = isAdmin ? ADMIN_TYPE : USER_TYPE;
        
        if (isAdmin) {
            Admin admin = (Admin) user;
            userId = admin.getId();
            StpUtil.login(userId, userType);
            StpUtil.getSession().set("admin", admin);
        } else {
            User normalUser = (User) user;
            userId = normalUser.getId();
            StpUtil.login(userId, userType);
            StpUtil.getSession().set("user", normalUser);
        }
        
        return StpUtil.getTokenValue();
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户ID（兼容旧版本）
     * @return 用户ID
     */
    public static Long getLoginId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    public static User getCurrentUser() {
        return (User) StpUtil.getSession().get("user");
    }

    /**
     * 获取当前登录管理员信息
     * @return 管理员信息
     */
    public static Admin getCurrentAdmin() {
        return (Admin) StpUtil.getSession().get("admin");
    }

    /**
     * 检查是否登录（用户）
     * @return 是否登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin() && USER_TYPE.equals(StpUtil.getLoginType());
    }

    /**
     * 检查管理员是否登录
     * @return 是否登录
     */
    public static boolean isAdminLogin() {
        return StpUtil.isLogin() && ADMIN_TYPE.equals(StpUtil.getLoginType());
    }

    /**
     * 用户登出
     */
    public static void logout() {
        logout(false);
    }

    /**
     * 登出方法（支持区分用户类型）
     * @param isAdmin 是否为管理员登出
     */
    public static void logout(boolean isAdmin) {
        String loginType = isAdmin ? ADMIN_TYPE : USER_TYPE;
        String currentType = StpUtil.getLoginType();

        // 只有当前登录类型匹配时才执行登出
        if (loginType.equals(currentType)) {
            StpUtil.logout();
        }
    }

    /**
     * 获取当前登录类型
     * @return 登录类型（"user" 或 "admin"）
     */
    public static String getCurrentLoginType() {
        return StpUtil.getLoginType();
    }

    /**
     * 检查当前用户是否具有指定角色
     * @param role 角色标识
     * @return 是否具有该角色
     */
    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }

    /**
     * 检查当前用户是否具有指定权限
     * @param permission 权限标识
     * @return 是否具有该权限
     */
    public static boolean hasPermission(String permission) {
        return StpUtil.hasPermission(permission);
    }

    /**
     * 获取当前用户的角色列表
     * @return 角色列表
     */
    public static List<String> getRoleList() {
        return StpUtil.getRoleList();
    }

    /**
     * 获取当前用户的权限列表
     * @return 权限列表
     */
    public static List<String> getPermissionList() {
        return StpUtil.getPermissionList();
    }

    /**
     * 获取当前token值
     * @return token值
     */
    public static String getToken() {
        return StpUtil.getTokenValue();
    }

    /**
     * 检查指定用户是否登录
     * @param userId 用户ID
     * @return 是否登录
     */
    public static boolean isLogin(Long userId) {
        return StpUtil.isLogin(userId);
    }

    /**
     * 获取指定用户的token值
     * @param userId 用户ID
     * @return token值
     */
    public static String getToken(Long userId) {
        return StpUtil.getTokenValueByLoginId(userId);
    }
}