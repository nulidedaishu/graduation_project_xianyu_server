package xyz.yaungyue.secondhand.exception;

import lombok.Getter;

/**
 * 服务层基础异常类
 * 所有业务异常都应该继承此类
 */
@Getter
public class ServiceException extends RuntimeException {
    
    /**
     * 错误码
     */
    protected Integer code;
    
    /**
     * 错误信息
     */
    protected String message;
    
    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
        this.message = message;
    }
    
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
    
    public ServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.message = message;
    }
    
    public ServiceException(Integer code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.message = message;
    }
}
