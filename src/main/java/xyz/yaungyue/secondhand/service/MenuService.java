package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yaung
* @description 针对表【sys_menu(权限菜单表)】的数据库操作Service
* @createDate 2026-02-12 17:21:09
*/
public interface MenuService extends IService<Menu> {

    /**
     * 根据管理员ID获取其所有菜单权限（递归加载所有子菜单）
     * @param adminId 管理员ID
     * @return 菜单列表
     */
    List<Menu> getMenusByAdminId(Long adminId);
}
