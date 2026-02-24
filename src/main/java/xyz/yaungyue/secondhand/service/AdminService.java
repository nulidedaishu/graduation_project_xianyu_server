package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yaung
* @description 针对表【sys_admin(管理员表)】的数据库操作Service
* @createDate 2026-02-24 00:00:00
*/
public interface AdminService extends IService<Admin> {

    /**
     * 根据用户名查找管理员
     * @param username 用户名
     * @return 管理员信息
     */
    Admin findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
}