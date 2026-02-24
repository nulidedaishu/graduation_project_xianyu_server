package xyz.yaungyue.secondhand.model.dto.response;

import java.io.Serial;
import java.io.Serializable;


public record ApiResponse<T>(int code, String message, T data) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(200, "Success", null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
