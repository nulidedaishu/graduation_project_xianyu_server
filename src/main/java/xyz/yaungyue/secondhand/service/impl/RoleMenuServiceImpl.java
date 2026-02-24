package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.RoleMenu;
import xyz.yaungyue.secondhand.service.RoleMenuService;
import xyz.yaungyue.secondhand.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【sys_role_menu(角色权限关联表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:09
*/
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu>
    implements RoleMenuService{

}




