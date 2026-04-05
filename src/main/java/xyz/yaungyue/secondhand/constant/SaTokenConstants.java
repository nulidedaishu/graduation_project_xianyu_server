package xyz.yaungyue.secondhand.constant;

/**
 * Sa-Token 常量类
 * 定义登录类型、角色、权限等常量，用于统一Sa-Token注解使用
 */
public class SaTokenConstants {

    /**
     * 登录类型常量
     */
    public static final String LOGIN_TYPE_USER = "user";
    public static final String LOGIN_TYPE_ADMIN = "admin";

    /**
     * 角色标识常量（与Sa-Token中使用的角色代码一致）
     */
    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    /**
     * 权限标识常量
     */
    public static final String PERMISSION_USER_PUBLISH = "product:publish";
    public static final String PERMISSION_USER_BUY = "product:buy";
    public static final String PERMISSION_USER_CREATE_ORDER = "order:create";
    public static final String PERMISSION_USER_VIEW_ORDER = "order:view";
    public static final String PERMISSION_USER_SEND_MESSAGE = "message:send";
    public static final String PERMISSION_USER_UPDATE = "user:update";

    /**
     * 路径模式常量
     */
    public static final String ADMIN_PATH_PREFIX = "/api/admin/";
    public static final String AUTH_PATH_PREFIX = "/api/auth/";

    /**
     * 私有构造方法，防止实例化
     */
    private SaTokenConstants() {
        throw new IllegalStateException("常量类不能实例化");
    }
}