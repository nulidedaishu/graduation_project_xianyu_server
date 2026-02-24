package xyz.yaungyue.secondhand.model.dto.response;

public record LoginResponse(
    String token,
    UserInfo user
) {
    public record UserInfo(
        Long id,
        String username,
        String nickname,
        String avatar,
        String phone
    ) {
    }
}