package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.yaungyue.secondhand.mapper.AdminRoleMapper;
import xyz.yaungyue.secondhand.mapper.RoleMapper;
import xyz.yaungyue.secondhand.mapper.UserRoleMapper;
import xyz.yaungyue.secondhand.model.entity.AdminRole;
import xyz.yaungyue.secondhand.model.entity.Role;
import xyz.yaungyue.secondhand.model.entity.UserRole;
import xyz.yaungyue.secondhand.service.RoleService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final UserRoleMapper userRoleMapper;
    private final AdminRoleMapper adminRoleMapper;

    @Override
    public List<String> getRolesByUserId(Long userId) {
        // 1. 查询用户角色关联表
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);

        if (userRoles.isEmpty()) {
            // 默认返回普通用户角色
            return List.of("user");
        }

        // 2. 查询角色详情
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = this.listByIds(roleIds);

        // 3. 返回角色code列表
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        // 如果没有角色，添加默认角色
        if (roleCodes.isEmpty()) {
            roleCodes = List.of("user");
        }

        return roleCodes;
    }

    @Override
    public List<String> getRolesByAdminId(Long adminId) {
        // 1. 查询管理员角色关联表
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminRole::getAdminId, adminId);
        List<AdminRole> adminRoles = adminRoleMapper.selectList(wrapper);

        if (adminRoles.isEmpty()) {
            // 默认返回管理员角色
            return List.of("admin");
        }

        // 2. 查询角色详情
        List<Long> roleIds = adminRoles.stream()
                .map(AdminRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = this.listByIds(roleIds);

        // 3. 返回角色code列表
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        // 添加管理员角色标识
        if (!roleCodes.contains("admin")) {
            roleCodes = new java.util.ArrayList<>(roleCodes);
            roleCodes.add("admin");
        }

        return roleCodes;
    }
}
