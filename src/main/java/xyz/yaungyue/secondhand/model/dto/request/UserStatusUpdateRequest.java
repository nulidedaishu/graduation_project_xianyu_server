package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新请求
 */
@Data
public class UserStatusUpdateRequest {

    /**
     * 用户状态（0-禁用，1-正常）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
