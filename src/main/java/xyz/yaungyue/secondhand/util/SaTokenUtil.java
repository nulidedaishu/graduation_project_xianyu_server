package xyz.yaungyue.secondhand.util;

import cn.dev33.satoken.stp.StpUtil;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.model.entity.Admin;

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
        // 根据登录类型登出指定会话
        // 注意：Sa-Token的logoutByLoginType方法可能需要特定版本支持
        // 如果不可用，可以通过其他方式实现类型区分
        StpUtil.logout();
        // TODO: 实现真正的按类型登出逻辑
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