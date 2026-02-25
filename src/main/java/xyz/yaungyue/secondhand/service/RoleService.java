package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yaung
* @description 针对表【sys_role(角色表)】的数据库操作Service
* @createDate 2026-02-12 17:21:09
*/
public interface RoleService extends IService<Role> {

    /**
     * 根据用户ID获取角色列表
     * @param userId 用户ID
     * @return 角色code列表
     */
    List<String> getRolesByUserId(Long userId);

    /**
     * 根据管理员ID获取角色列表
     * @param adminId 管理员ID
     * @return 角色code列表
     */
    List<String> getRolesByAdminId(Long adminId);
}
