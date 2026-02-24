package xyz.yaungyue.secondhand.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * 规则：
 * 1xxx-2xxx: HTTP状态码对应错误
 * 3xxxx: 系统通用错误
 * 4xxxx: 业务模块错误（用户模块：401xx，订单模块：402xx...）
 */
@Getter
public enum ErrorCode {

    // ========== HTTP状态码对应错误（1xxx-2xxx）==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    UNPROCESSABLE_ENTITY(422, "业务处理失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    // ========== 系统通用错误（3xxxx）==========
    DUPLICATE_DATA(30001, "数据已存在"),
    DATA_INTEGRITY_VIOLATION(30002, "数据操作违反约束"),
    DATA_NOT_FOUND(30003, "数据不存在"),
    PARAM_VALIDATE_FAILED(30004, "参数校验失败"),
    REMOTE_SERVICE_ERROR(30005, "远程服务调用失败"),

    // ========== 业务模块错误 ==========
    // 用户模块（401xx）
    USER_NOT_FOUND(40101, "用户不存在"),
    USER_DISABLED(40102, "用户已被禁用"),
    USER_PASSWORD_ERROR(40103, "密码错误"),
    USER_PHONE_EXISTS(40104, "手机号已存在"),
    USER_EMAIL_EXISTS(40105, "邮箱已存在"),
    USER_USERNAME_EXISTS(40106, "用户名已存在"),
    USER_PASSWORD_MISMATCH(40107, "两次输入的密码不一致"),

    // 订单模块（402xx）
    ORDER_NOT_FOUND(40201, "订单不存在"),
    ORDER_STATUS_ERROR(40202, "订单状态异常"),
    ORDER_PAID(40203, "订单已支付"),

    // 商品模块（403xx）
    PRODUCT_NOT_FOUND(40301, "商品不存在"),
    PRODUCT_STOCK_INSUFFICIENT(40302, "商品库存不足"),

    // 支付模块（404xx）
    PAYMENT_FAILED(40401, "支付失败"),
    PAYMENT_TIMEOUT(40402, "支付超时"),

    // 商品分类模块（405xx）
    CATEGORY_NOT_FOUND(40501, "分类不存在"),
    CATEGORY_PARENT_NOT_FOUND(40502, "父分类不存在"),
    CATEGORY_HAS_CHILDREN(40503, "该分类下存在子分类，无法删除"),
    CATEGORY_USED_BY_PRODUCT(40504, "该分类正在被商品使用，无法删除"),
    CATEGORY_NAME_EXISTS(40505, "同级分类名称已存在"),

    // 商品模块（406xx）
    PRODUCT_CREATE_FAILED(40601, "商品创建失败"),
    PRODUCT_UPDATE_FAILED(40602, "商品更新失败"),
    PRODUCT_DELETE_FAILED(40603, "商品删除失败"),
    INVALID_PRODUCT_STATUS(422, "无效的商品状态");

    private final int code;
    private final String message;

    // 构造函数
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}