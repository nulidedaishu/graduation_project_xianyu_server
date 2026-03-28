package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.yaungyue.secondhand.constant.UserStatus;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.mapper.UserMapper;
import xyz.yaungyue.secondhand.model.dto.request.UserQueryRequest;
import xyz.yaungyue.secondhand.model.dto.response.PageResponse;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.UserManageService;

import java.util.List;

/**
 * 用户管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    private final UserMapper userMapper;

    /**
     * 获取用户列表（分页）
     */
    @Override
    public PageResponse<User> getUserList(UserQueryRequest request) {
        // 1. 构建分页对象
        Page<User> page = new Page<>(request.getPage(), request.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（用户名、昵称、手机号）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, request.getKeyword())
                    .or()
                    .like(User::getNickname, request.getKeyword())
                    .or()
                    .like(User::getPhone, request.getKeyword()));
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        // 3. 执行查询
        Page<User> resultPage = userMapper.selectPage(page, wrapper);

        // 4. 构建响应
        return PageResponse.of(
                resultPage.getRecords(),
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize()
        );
    }

    /**
     * 获取用户详情
     */
    @Override
    public User getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 隐藏敏感信息
        user.setPassword(null);
        return user;
    }

    /**
     * 启用/禁用用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        // 1. 验证状态值
        if (!UserStatus.isValid(status)) {
            throw new BusinessException(400, "无效的用户状态");
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 3. 更新状态
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);

        int rows = userMapper.updateById(updateUser);

        if (rows > 0) {
            log.info("用户状态更新成功，用户ID: {}, 新状态: {}", userId, status);
            return true;
        }

        return false;
    }

    /**
     * 获取所有用户（不分页）
     */
    @Override
    public List<User> getAllUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        List<User> users = userMapper.selectList(wrapper);
        // 隐藏敏感信息
        users.forEach(user -> user.setPassword(null));
        return users;
    }
}
