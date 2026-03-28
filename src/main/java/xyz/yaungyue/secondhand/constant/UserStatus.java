package xyz.yaungyue.secondhand.constant;

/**
 * 用户状态常量
 */
public class UserStatus {

    /**
     * 禁用
     */
    public static final int DISABLED = 0;

    /**
     * 正常
     */
    public static final int ENABLED = 1;

    /**
     * 获取状态描述
     * @param status 状态码
     * @return 状态描述
     */
    public static String getDescription(int status) {
        return switch (status) {
            case DISABLED -> "禁用";
            case ENABLED -> "正常";
            default -> "未知状态";
        };
    }

    /**
     * 检查状态是否有效
     * @param status 状态码
     * @return 是否有效
     */
    public static boolean isValid(int status) {
        return status == DISABLED || status == ENABLED;
    }
}
