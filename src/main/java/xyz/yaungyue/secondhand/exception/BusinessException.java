package xyz.yaungyue.secondhand.exception;

import lombok.Getter;

/**
 * 业务逻辑异常类
 * 用于处理业务层面的异常情况，如数据不存在、状态异常等
 */
@Getter
public class BusinessException extends ServiceException {
    
    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public BusinessException(Integer code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
    
    /**
     * 根据错误码枚举创建异常
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
    
    /**
     * 根据错误码枚举和原因创建异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), cause);
    }
}
