package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.yaungyue.secondhand.constant.SystemConstants;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.mapper.UserMapper;
import xyz.yaungyue.secondhand.model.dto.request.RegisterRequest;
import xyz.yaungyue.secondhand.model.dto.response.LoginResponse;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.model.entity.UserRole;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.service.UserRoleService;
import xyz.yaungyue.secondhand.util.JwtUtil;

/**
* @author yaung
* @description 针对表【sys_user(用户基础表)】的数据库操作Service实现
* @createDate 2026-02-08 02:00:58
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRoleService userRoleService;

    @Override
    public LoginResponse login(String username, String password) {
        // 查找用户
        User user = findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 构造用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatar(),
            user.getPhone()
        );

        return new LoginResponse(token, userInfo);
    }

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public User findById(Long userId) {
        return getById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return count(queryWrapper) > 0;
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        // 参数校验
        if (!registerRequest.isPasswordMatch()) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_MISMATCH);
        }

        // 检查用户名是否已存在
        if (existsByUsername(registerRequest.username())) {
            throw new BusinessException(ErrorCode.USER_USERNAME_EXISTS);
        }

        // 如果提供了手机号，检查手机号是否已存在
        if (registerRequest.phone() != null && !registerRequest.phone().isEmpty()) {
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone", registerRequest.phone());
            if (count(phoneQuery) > 0) {
                throw new BusinessException(ErrorCode.USER_PHONE_EXISTS);
            }
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setNickname(registerRequest.nickname());
        user.setPhone(registerRequest.phone());
        user.setCredit_score(SystemConstants.DEFAULT_CREDIT_SCORE); // 默认信用积分
        user.setStatus(SystemConstants.USER_STATUS_NORMAL); // 默认状态为正常
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreate_time(now);
        user.setUpdate_time(now);

        // 保存用户
        if (!save(user)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 为新用户分配默认角色(ROLE_USER)
        UserRole userRole = new UserRole();
        userRole.setUser_id(user.getId());
        userRole.setRole_id(SystemConstants.ROLE_USER);
        if (!userRoleService.save(userRole)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 返回不包含密码的用户信息
        user.setPassword(null);
        return user;
    }
    
    @Override
    public Set<Long> getUserRoleIds(Long userId) {
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserRole> userRoles = userRoleService.list(queryWrapper);
        
        // 过滤掉null元素，避免NullPointerException
        return userRoles.stream()
            .filter(Objects::nonNull)
            .map(UserRole::getRole_id)
            .filter(Objects::nonNull) // 同时过滤掉role_id为null的情况
            .collect(Collectors.toSet());
    }
    
    @Override
    public boolean hasRole(Long userId, Long roleId) {
        Set<Long> roleIds = getUserRoleIds(userId);
        return roleIds.contains(roleId);
    }
    
    @Override
    public boolean validateUserLoginType(Long userId, boolean expectedHasUserRole) {
        Set<Long> userRoles = getUserRoleIds(userId);
        boolean hasUserRole = userRoles.contains(SystemConstants.ROLE_USER);
        
        return hasUserRole == expectedHasUserRole;
    }
}