package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.AdminRole;
import xyz.yaungyue.secondhand.service.AdminRoleService;
import xyz.yaungyue.secondhand.mapper.AdminRoleMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【sys_admin_role(管理员角色关联表)】的数据库操作Service实现
* @createDate 2026-02-24 18:54:23
*/
@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole>
    implements AdminRoleService{

}




