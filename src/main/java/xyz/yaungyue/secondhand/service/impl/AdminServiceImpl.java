package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.yaungyue.secondhand.mapper.AdminMapper;
import xyz.yaungyue.secondhand.model.entity.Admin;
import xyz.yaungyue.secondhand.service.AdminService;

/**
* @author yaung
* @description 针对表【sys_admin(管理员表)】的数据库操作Service实现
* @createDate 2026-02-24 00:00:00
*/
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{
    
    private final PasswordEncoder passwordEncoder;

    @Override
    public Admin findByUsername(String username) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return count(queryWrapper) > 0;
    }
}