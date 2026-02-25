package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.yaungyue.secondhand.constant.ProductStatus;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.service.ProductService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductDeleteTest {

    @Autowired
    private ProductService productService;

    @Test
    public void testDeleteProductMethodExists() {
        // 验证删除方法在接口中存在
        try {
            ProductService.class.getMethod("deleteProduct", Long.class, Long.class);
            System.out.println("✓ ProductService.deleteProduct(Long, Long) 方法存在");
        } catch (NoSuchMethodException e) {
            System.out.println("✗ ProductService.deleteProduct(Long, Long) 方法不存在");
            fail("删除方法未在接口中声明");
        }
    }

    @Test
    public void testProductStatusConstants() {
        // 验证商品状态常量
        assertEquals(0, ProductStatus.PENDING, "待审核状态应为0");
        assertEquals(1, ProductStatus.APPROVED, "已上架状态应为1");
        assertEquals(2, ProductStatus.REJECTED, "审核驳回状态应为2");
        assertEquals(3, ProductStatus.OFFLINE, "已下架状态应为3");
        assertEquals(4, ProductStatus.SOLD, "已售出状态应为4");
        assertEquals(5, ProductStatus.DELETED, "已删除状态应为5");
        
        System.out.println("✓ 商品状态常量验证通过");
    }

    @Test
    public void testProductStatusDescriptions() {
        // 验证状态描述
        assertEquals("待审核", ProductStatus.getDescription(ProductStatus.PENDING));
        assertEquals("已上架", ProductStatus.getDescription(ProductStatus.APPROVED));
        assertEquals("审核驳回", ProductStatus.getDescription(ProductStatus.REJECTED));
        assertEquals("已下架", ProductStatus.getDescription(ProductStatus.OFFLINE));
        assertEquals("已售出", ProductStatus.getDescription(ProductStatus.SOLD));
        assertEquals("已删除", ProductStatus.getDescription(ProductStatus.DELETED));
        
        System.out.println("✓ 商品状态描述验证通过");
    }

    @Test
    public void testProductEditableStatus() {
        // 验证哪些状态允许编辑（这些状态也应该是可以删除的）
        assertTrue(ProductStatus.isEditable(ProductStatus.PENDING), "待审核状态应该允许编辑");
        assertTrue(ProductStatus.isEditable(ProductStatus.REJECTED), "审核驳回状态应该允许编辑");
        assertTrue(ProductStatus.isEditable(ProductStatus.OFFLINE), "已下架状态应该允许编辑");
        assertFalse(ProductStatus.isEditable(ProductStatus.APPROVED), "已上架状态不应该允许编辑");
        assertFalse(ProductStatus.isEditable(ProductStatus.SOLD), "已售出状态不应该允许编辑");
        assertFalse(ProductStatus.isEditable(ProductStatus.DELETED), "已删除状态不应该允许编辑");
        
        System.out.println("✓ 商品可编辑状态验证通过");
    }
}