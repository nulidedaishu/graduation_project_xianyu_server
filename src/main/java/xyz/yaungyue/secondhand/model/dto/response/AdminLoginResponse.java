package xyz.yaungyue.secondhand.model.dto.response;

import xyz.yaungyue.secondhand.model.entity.Admin;

/**
 * 管理员登录响应
 */
public record AdminLoginResponse(
    String token,
    Admin admin
) {}