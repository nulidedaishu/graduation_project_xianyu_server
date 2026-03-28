package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.request.AdminLoginRequest;
import xyz.yaungyue.secondhand.model.dto.request.LoginRequest;
import xyz.yaungyue.secondhand.model.dto.request.RegisterRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.LoginResponse;
import xyz.yaungyue.secondhand.model.entity.Admin;
import xyz.yaungyue.secondhand.model.entity.Menu;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.AdminService;
import xyz.yaungyue.secondhand.service.MenuService;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.JwtUtil;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 提供用户和管理员的登录、注册、登出等功能
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、登出等认证接口")
public class AuthController {

    private final UserService userService;
    private final AdminService adminService;
    private final MenuService menuService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String ADMIN_MENU_CACHE_PREFIX = "admin:menus:";
    private static final long CACHE_EXPIRE_HOURS = 2;

    /**
     * 用户登录
     * @param request 登录请求参数
     * @return 登录响应，包含 token 和用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "普通用户使用账号密码登录，返回 Sa-Token 和用户信息")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        // 使用原有的登录逻辑验证用户
        LoginResponse response = userService.login(request.username(), request.password());
        
        // 使用Sa-Token进行会话管理
        User user = userService.findByUsername(request.username());
        
        // 验证用户登录类型（必须包含ROLE_USER角色）
        if (!userService.validateUserLoginType(user.getId(), true)) {
            throw new BusinessException(401, "无对应账号");
        }
        
        String saToken = SaTokenUtil.login(user);
        
        // 将Sa-Token添加到响应中
        LoginResponse enhancedResponse = new LoginResponse(
            saToken, // 使用Sa-Token作为主要token
            response.user()
        );
        
        return ApiResponse.success(enhancedResponse);
    }

    /**
     * 管理员登录
     * @param request 管理员登录请求参数
     * @return 登录响应，包含 token 和管理员信息
     */
    @PostMapping("/admin/login")
    @Operation(summary = "管理员登录", description = "管理员使用账号密码登录，返回 Sa-Token 和管理员信息")
    public ApiResponse<LoginResponse> adminLogin(@RequestBody @Valid AdminLoginRequest request) {
        try {
            // 验证管理员账号密码
            Admin admin = adminService.findByUsername(request.username());
            if (admin == null) {
                throw new BusinessException(401, "无对应账号");
            }
            
            // 检查管理员状态
            if (admin.getStatus() == 0) {
                throw new BusinessException(401, "账号已被禁用");
            }
            // if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            //     throw new BusinessException(401, "密码错误");
            // }
            
            // 验证管理员登录类型（不能包含ROLE_USER角色）
            // 这里假设管理员通过Admin表的role字段来区分，而不是UserRole表
            // 如果需要通过UserRole表验证，则需要相应的查询逻辑
            
            // 使用Sa-Token进行管理员会话管理
            String saToken = SaTokenUtil.login(admin, true);
            
            // 加载并缓存管理员菜单权限
            List<Menu> menus = menuService.getMenusByAdminId(admin.getId());
            String cacheKey = ADMIN_MENU_CACHE_PREFIX + admin.getId();
            redisTemplate.opsForValue().set(cacheKey, menus, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 构造响应
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                admin.getId(),
                admin.getUsername(),
                admin.getRealName(),
                null, // 管理员可能没有头像
                null  // 管理员可能没有手机号
            );
            
            LoginResponse loginResponse = new LoginResponse(saToken, userInfo);
            return ApiResponse.success(loginResponse);
            
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 用户登出
     * @param request HTTP 请求
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录，清除 Sa-Token 会话")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        // Sa-Token登出（用户）
        SaTokenUtil.logout();
        return ApiResponse.success();
    }
    
    /**
     * 管理员登出
     * @param request HTTP 请求
     * @return 操作结果
     */
    @PostMapping("/admin/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录，清除 Sa-Token 会话")
    public ApiResponse<Void> adminLogout(HttpServletRequest request) {
        // Sa-Token登出（管理员）
        SaTokenUtil.logout(true);
        return ApiResponse.success();
    }

    /**
     * 获取当前登录用户信息
     * @return 当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息，需要登录")
    public ApiResponse<User> getUserInfo() {
        // 获取当前登录用户信息
        User currentUser = SaTokenUtil.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        return ApiResponse.success(currentUser);
    }
    
    /**
     * 获取当前登录管理员信息
     * @return 当前管理员信息
     */
    @GetMapping("/admin/info")
    @Operation(summary = "获取当前管理员信息", description = "获取当前登录管理员的详细信息，需要管理员登录")
    public ApiResponse<Admin> getAdminInfo() {
        // 获取当前登录管理员信息
        Admin currentAdmin = SaTokenUtil.getCurrentAdmin();
        if (currentAdmin == null) {
            return ApiResponse.error(401, "管理员未登录");
        }
        return ApiResponse.success(currentAdmin);
    }

    /**
     * 检查用户是否已登录
     * @return true-已登录，false-未登录
     */
    @GetMapping("/check-login")
    @Operation(summary = "检查用户登录状态", description = "检查当前用户是否已登录，返回 true/false")
    public ApiResponse<Boolean> checkLogin() {
        // 检查是否登录
        return ApiResponse.success(SaTokenUtil.isLogin());
    }
    
    /**
     * 检查管理员是否已登录
     * @return true-已登录，false-未登录
     */
    @GetMapping("/admin/check-login")
    @Operation(summary = "检查管理员登录状态", description = "检查当前管理员是否已登录，返回 true/false")
    public ApiResponse<Boolean> checkAdminLogin() {
        // 检查管理员是否登录
        return ApiResponse.success(SaTokenUtil.isAdminLogin());
    }

    /**
     * 用户注册
     * @param request 注册请求参数
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，需要提供用户名、密码等信息")
    public ApiResponse<User> register(@RequestBody @Valid RegisterRequest request) {
        try {
            // 执行注册
            User user = userService.register(request);
            return ApiResponse.success(user);
        } catch (BusinessException e) {
            // 捕获业务异常，返回带有错误码的成功响应
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }
}