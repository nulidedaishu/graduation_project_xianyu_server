package xyz.yaungyue.secondhand.exception;

import lombok.Getter;

/**
 * 权限认证异常类
 * 用于处理身份认证和权限验证相关的异常
 */
@Getter
public class UnauthorizedException extends ServiceException {
    
    public UnauthorizedException() {
        super(401, "未授权，请先登录");
    }

    public UnauthorizedException(String message) {
        super(401, message);
    }
    
    public UnauthorizedException(Integer code, String message) {
        super(code, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(401, message, cause);
    }
    
    public UnauthorizedException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(401, "未授权，请先登录", cause);
    }

    public UnauthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(401, message, cause, enableSuppression, writableStackTrace);
    }
    
    public UnauthorizedException(Integer code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
    
    /**
     * 根据错误码枚举创建权限异常
     */
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    
    /**
     * 根据错误码枚举和原因创建权限异常
     */
    public UnauthorizedException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
}