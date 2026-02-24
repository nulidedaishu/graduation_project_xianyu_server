package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;

/**
 * 用户管理控制器
 * 演示Sa-Token权限控制功能
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表（需要登录）
     */
    @GetMapping
    @SaCheckLogin  // 需要登录才能访问
    public ApiResponse<List<User>> getUserList() {
        // TODO: 实现分页查询
        List<User> users = userService.list();
        return ApiResponse.success(users);
    }

    /**
     * 获取用户详情（需要登录）
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(user);
    }

    /**
     * 更新用户信息（需要登录且只能更新自己的信息）
     */
    @PutMapping("/{id}")
    @SaCheckLogin
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        // 检查是否是本人操作
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            return ApiResponse.error(403, "只能更新自己的信息");
        }

        user.setId(id);
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
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")  // 需要admin角色
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "删除失败");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    @SaCheckLogin
    public ApiResponse<User> getCurrentUser() {
        User currentUser = SaTokenUtil.getCurrentUser();
        return ApiResponse.success(currentUser);
    }

    /**
     * 刷新用户session信息
     */
    @PostMapping("/refresh-session")
    @SaCheckLogin
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