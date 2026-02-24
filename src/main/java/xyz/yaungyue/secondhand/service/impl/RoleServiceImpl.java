package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Role;
import xyz.yaungyue.secondhand.service.RoleService;
import xyz.yaungyue.secondhand.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【sys_role(角色表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:09
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




