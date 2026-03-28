package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.UserQueryRequest;
import xyz.yaungyue.secondhand.model.dto.response.PageResponse;
import xyz.yaungyue.secondhand.model.entity.User;

import java.util.List;

/**
 * 用户管理服务
 */
public interface UserManageService {

    /**
     * 获取用户列表（分页）
     */
    PageResponse<User> getUserList(UserQueryRequest request);
    
    /**
     * 获取用户详情
     */
    User getUserDetail(Long userId);
    
    /**
     * 启用/禁用用户
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 获取所有用户（不分页）
     */
    List<User> getAllUsers();
}