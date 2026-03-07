package xyz.yaungyue.secondhand.constant;

/**
 * 系统常量类
 */
public class SystemConstants {
    
    /**
     * 角色相关常量
     */
    public static final Long ROLE_ADMIN = 1L;
    public static final Long ROLE_USER = 2L;
    
    /**
     * 用户状态常量
     */
    public static final Integer USER_STATUS_NORMAL = 1;
    public static final Integer USER_STATUS_DISABLED = 0;
    
    /**
     * 默认信用积分
     */
    public static final Integer DEFAULT_CREDIT_SCORE = 100;
    
    /**
     * 文件业务类型常量
     */
    // 商品相关
    public static final Integer FILE_BIZ_TYPE_PRODUCT = 1;      // 商品信息
    public static final Integer FILE_BIZ_TYPE_MAIN_IMAGE = 8;   // 商品主图
}