package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员登录请求
 */
public record AdminLoginRequest(
    @NotBlank(message = "用户名不能为空")
    String username,
    
    @NotBlank(message = "密码不能为空")
    String password
) {}