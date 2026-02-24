package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import xyz.yaungyue.secondhand.model.entity.Admin;
import xyz.yaungyue.secondhand.model.entity.AdminRole;
import xyz.yaungyue.secondhand.model.entity.Menu;
import xyz.yaungyue.secondhand.model.entity.RoleMenu;
import xyz.yaungyue.secondhand.service.AdminService;
import xyz.yaungyue.secondhand.service.AdminRoleService;
import xyz.yaungyue.secondhand.service.MenuService;
import xyz.yaungyue.secondhand.service.RoleMenuService;
import xyz.yaungyue.secondhand.mapper.MenuMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author yaung
* @description 针对表【sys_menu(权限菜单表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:09
*/
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu>
    implements MenuService{
    
    private final AdminService adminService;
    private final AdminRoleService adminRoleService;
    private final RoleMenuService roleMenuService;

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
            .map(AdminRole::getRole_id)
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
            .map(RoleMenu::getMenu_id)
            .distinct()
            .collect(Collectors.toList());
        
        // 6. 查询所有相关菜单
        List<Menu> menus = listByIds(menuIds);
        
        // 7. 构建菜单树结构（递归加载所有子菜单）
        return buildMenuTree(menus, 0L);
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
            if (parentId.equals(menu.getParent_id())) {
                // 递归查找子菜单
                List<Menu> children = buildMenuTree(menus, menu.getId());
                // 这里可以根据需要设置子菜单属性
                // menu.setChildren(children);
                tree.add(menu);
            }
        }
        
        return tree;
    }
}




