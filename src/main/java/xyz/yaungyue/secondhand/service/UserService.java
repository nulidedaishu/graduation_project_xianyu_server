package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.RegisterRequest;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.model.dto.response.LoginResponse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**
* @author yaung
* @description 针对表【sys_user(用户基础表)】的数据库操作Service
* @createDate 2026-02-08 02:00:58
*/
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录响应信息
     */
    LoginResponse login(String username, String password);

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(String username);

    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户实体
     */
    User findById(Long userId);

    /**
     * 检查用户是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册成功的用户信息
     */
    User register(RegisterRequest registerRequest);
    
    /**
     * 获取用户的角色集合
     * @param userId 用户ID
     * @return 角色ID集合
     */
    Set<Long> getUserRoleIds(Long userId);
    
    /**
     * 验证用户是否具有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否具有该角色
     */
    boolean hasRole(Long userId, Long roleId);
    
    /**
     * 验证用户登录类型合法性
     * @param userId 用户ID
     * @param expectedHasUserRole 期望是否包含USER_ROLE
     * @return 验证结果
     */
    boolean validateUserLoginType(Long userId, boolean expectedHasUserRole);
}