package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 20, message = "昵称长度必须在 2-20 个字符之间")
        String nickname,

        @Pattern(regexp = "^https?://.*", message = "头像必须是有效的 URL")
        String avatar,

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone
) {}


