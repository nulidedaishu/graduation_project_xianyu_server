package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.yaungyue.secondhand.mapper.AdminRoleMapper;
import xyz.yaungyue.secondhand.mapper.MenuMapper;
import xyz.yaungyue.secondhand.mapper.RoleMenuMapper;
import xyz.yaungyue.secondhand.model.entity.Admin;
import xyz.yaungyue.secondhand.model.entity.AdminRole;
import xyz.yaungyue.secondhand.model.entity.Menu;
import xyz.yaungyue.secondhand.model.entity.RoleMenu;
import xyz.yaungyue.secondhand.service.AdminRoleService;
import xyz.yaungyue.secondhand.service.AdminService;
import xyz.yaungyue.secondhand.service.MenuService;
import xyz.yaungyue.secondhand.service.RoleMenuService;
import xyz.yaungyue.secondhand.service.RoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author yaung
* @description 针对表【sys_menu(权限菜单表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:09
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu>
    implements MenuService{

    private final AdminService adminService;
    private final AdminRoleService adminRoleService;
    private final RoleMenuService roleMenuService;
    private final AdminRoleMapper adminRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleService roleService;

    @Override
    public List<Menu> getMenusByAdminId(Long adminId) {
        // 1. 获取管理员信息
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            return new ArrayList<>();
        }

        // 2. 通过 adminId 查询 AdminRole 获取 roleId 列表
        QueryWrapper<AdminRole> adminRoleQuery = new QueryWrapper<>();
        adminRoleQuery.eq("admin_id", adminId);
        List<AdminRole> adminRoles = adminRoleService.list(adminRoleQuery);

        if (adminRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 提取所有角色ID
        List<Long> roleIds = adminRoles.stream()
            .map(AdminRole::getRoleId)
            .collect(Collectors.toList());

        // 4. 通过 roleId 列表查询 RoleMenu 获取所有菜单ID
        QueryWrapper<RoleMenu> roleMenuQuery = new QueryWrapper<>();
        roleMenuQuery.in("role_id", roleIds);
        List<RoleMenu> roleMenus = roleMenuService.list(roleMenuQuery);

        if (roleMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 5. 获取所有相关的菜单ID（去重）
        List<Long> menuIds = roleMenus.stream()
            .map(RoleMenu::getMenuId)
            .distinct()
            .collect(Collectors.toList());

        // 6. 查询所有相关菜单
        List<Menu> menus = listByIds(menuIds);

        // 7. 构建菜单树结构（递归加载所有子菜单）
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<String> getPermissionsByAdminId(Long adminId) {
        // 1. 查询管理员的角色关联
        LambdaQueryWrapper<AdminRole> adminRoleWrapper = new LambdaQueryWrapper<>();
        adminRoleWrapper.eq(AdminRole::getAdminId, adminId);
        List<AdminRole> adminRoles = adminRoleMapper.selectList(adminRoleWrapper);

        if (adminRoles.isEmpty()) {
            // 返回默认管理员权限
            return getDefaultAdminPermissions();
        }

        // 2. 查询角色对应的菜单权限
        List<Long> roleIds = adminRoles.stream()
                .map(AdminRole::getRoleId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
        roleMenuWrapper.in(RoleMenu::getRoleId, roleIds);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(roleMenuWrapper);

        if (roleMenus.isEmpty()) {
            return getDefaultAdminPermissions();
        }

        // 3. 查询菜单权限标识
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        List<Menu> menus = this.listByIds(menuIds);

        // 4. 提取权限标识
        List<String> permissions = menus.stream()
                .map(Menu::getPermission)
                .filter(permission -> permission != null && !permission.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 如果没有权限，添加默认权限
        if (permissions.isEmpty()) {
            permissions = getDefaultAdminPermissions();
        }

        return permissions;
    }

    /**
     * 构建菜单树结构
     * @param menus 所有菜单列表
     * @param parentId 父级ID
     * @return 树形结构的菜单列表
     */
    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        List<Menu> tree = new ArrayList<>();

        for (Menu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                // 递归查找子菜单
                List<Menu> children = buildMenuTree(menus, menu.getId());
                // 这里可以根据需要设置子菜单属性
                // menu.setChildren(children);
                tree.add(menu);
            }
        }

        return tree;
    }

    /**
     * 获取默认管理员权限
     */
    private List<String> getDefaultAdminPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add("product:audit");      // 商品审核
        permissions.add("product:delete");     // 删除商品
        permissions.add("user:manage");        // 用户管理
        permissions.add("order:manage");       // 订单管理
        permissions.add("category:manage");    // 分类管理
        permissions.add("system:config");      // 系统配置
        permissions.add("statistics:view");    // 查看统计
        return permissions;
    }
}
