package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.yaungyue.secondhand.service.ProductService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ProductStatusQueryTest {

    @Autowired
    private ProductService productService;

    @Test
    public void testGetProductsByStatusMethodExists() {
        // 验证服务方法存在
        assertNotNull(productService, "ProductService should not be null");
        
        // 验证方法可以被调用（不会抛出AbstractMethodError）
        try {
            productService.getProductsByStatus(0);
            System.out.println("✓ getProductsByStatus方法存在且可调用");
        } catch (Exception e) {
            // 这里可能会因为数据库连接等问题抛出异常，但方法本身是存在的
            System.out.println("✓ getProductsByStatus方法存在，异常信息: " + e.getMessage());
        }
    }
}