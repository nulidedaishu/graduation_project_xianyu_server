package xyz.yaungyue.secondhand.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，用于统一拦截并处理 Controller 层抛出的异常。
 * 返回结构化的错误信息（ApiResponse），避免直接暴露异常堆栈给客户端。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * 根据错误码获取对应的HTTP状态码
     */
    private HttpStatus getHttpStatusByCode(int errorCode) {
        // HTTP标准状态码范围
        if (errorCode >= 400 && errorCode < 500) {
            return switch (errorCode) {
                case 401 -> HttpStatus.UNAUTHORIZED;
                case 403 -> HttpStatus.FORBIDDEN;
                case 404 -> HttpStatus.NOT_FOUND;
                case 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
                case 429 -> HttpStatus.TOO_MANY_REQUESTS;
                default -> HttpStatus.BAD_REQUEST;
            };
        } else if (errorCode >= 500 && errorCode < 600) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        // 自定义业务错误码使用400
        return HttpStatus.BAD_REQUEST;
    }
    /**
     * 自定义权限异常（如权限不足），返回 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)    // 401
    public ApiResponse<?> handleUnauthorized(UnauthorizedException ex) {
        return ApiResponse.error(401, ex.getMessage());
    }

    /**
     * 自定义业务异常（如 BusinessException），返回 400
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)    // 400
    public ApiResponse<?> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    /**
     * 自定义业务异常（如 ServiceException），携带可定制的错误码
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)    // 400
    public ApiResponse<?> handleService(ServiceException e) {
        log.error("业务异常: code={}, msg={}", 400, e.getMessage(), e);
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 参数校验异常（@Valid 触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)    // 400
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    public ApiResponse<?> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * BindException 也可能用于 @Validated on @RequestBody 外的场景
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)    // 400
    public ApiResponse<?> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * 参数类型不匹配（如期望 Integer 但传入 String）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)    // 400
    public ApiResponse<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("参数 '%s' 类型错误，需要 %s 类型",
                ex.getName(),
                ex.getRequiredType() != null
                        ? ex.getRequiredType().getSimpleName()
                        : "未知");
        log.warn("参数类型错误: {}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * 运行时异常（NullPointerException、IllegalArgumentException 等）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)   // 400
    public ApiResponse<?> handleRuntime(RuntimeException e) {
        log.error("系统运行时异常", e);
        return ApiResponse.error(400,e.getMessage());
    }

    /**
     * 兜底：捕获所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)   // 500
    public ApiResponse<?> handleAll(Exception e) {
        log.error("系统未知异常", e);
        return ApiResponse.error(500, e.getMessage());
    }
}
