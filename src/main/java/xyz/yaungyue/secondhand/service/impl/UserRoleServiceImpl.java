package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.UserRole;
import xyz.yaungyue.secondhand.service.UserRoleService;
import xyz.yaungyue.secondhand.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【sys_user_role(用户角色关联表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:24
*/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
    implements UserRoleService{

}




