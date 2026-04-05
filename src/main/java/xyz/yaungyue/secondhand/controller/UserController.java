package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.request.UserRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;


/**
 * 用户管理控制器
 * 演示Sa-Token权限控制功能
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息查询、更新、删除等接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取用户详情（需要登录）
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = "user:user:detail", type = "user")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<User> getUserById(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息（需要登录且只能更新自己的信息）
     *
     * @param id          用户 ID
     * @param userRequest 用户修改信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    @SaCheckPermission(value = "user:user:update", type = "user")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息（只能更新自己的信息）")
    public ApiResponse<User> updateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long id,
            @RequestBody UserRequest userRequest) {
        // 检查是否是本人操作
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            return ApiResponse.error(403, "只能更新自己的信息");
        }
        User user = userService.findById(id);
        user.setNickname(userRequest.nickname());
        user.setAvatar(userRequest.avatar());
        user.setPhone(userRequest.phone());

        boolean success = userService.updateById(user);
        if (success) {
            // 更新session中的用户信息
            StpUtil.getSession().set("user", user);
            return ApiResponse.success(user);
        }
        return ApiResponse.error(500, "更新失败");
    }

    /**
     * 删除用户（需要管理员角色）
     *
     * @param id 用户 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission(value = "admin:user:delete", type = "admin")
    @Operation(summary = "删除用户", description = "删除指定用户，需要管理员权限")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "删除失败");
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @SaCheckPermission(value = "user:user:me", type = "user")
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<User> getCurrentUser() {
        User currentUser = SaTokenUtil.getCurrentUser();
        return ApiResponse.success(currentUser);
    }

    /**
     * 刷新用户 session 信息
     *
     * @return 刷新后的用户信息
     */
    @PostMapping("/refresh-session")
    @SaCheckPermission(value = "user:user:refresh", type = "user")
    @Operation(summary = "刷新用户会话", description = "刷新用户 session 中的信息")
    public ApiResponse<User> refreshSession() {
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        User user = userService.findById(currentUserId);
        if (user != null) {
            StpUtil.getSession().set("user", user);
            return ApiResponse.success(user);
        }
        return ApiResponse.error(404, "用户不存在");
    }
}