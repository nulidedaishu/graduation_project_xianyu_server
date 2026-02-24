package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.yaungyue.secondhand.model.entity.Menu;
import xyz.yaungyue.secondhand.service.MenuService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Test
    public void testGetMenusByAdminId() {
        // 测试不存在的管理员ID
        List<Menu> menus = menuService.getMenusByAdminId(-1L);
        assertNotNull(menus);
        assertTrue(menus.isEmpty());
        
        System.out.println("MenuService 修改验证通过：能够处理不存在的管理员ID");
    }
}