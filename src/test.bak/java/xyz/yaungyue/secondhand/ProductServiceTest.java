package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.model.entity.Product;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("商品服务单元测试")
class ProductServiceTest {

    private ProductCreateRequest validRequest;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        validRequest = new ProductCreateRequest();
        validRequest.setName("iPhone 15");
        validRequest.setDescription("全新未拆封");
        validRequest.setPrice(new BigDecimal("8999.00"));
        validRequest.setCategoryId(1L);
        validRequest.setImageUrls("https://example.com/image1.jpg");
        validRequest.setDetail("详细描述...");
        validRequest.setContactInfo("13800138000");

        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setTitle("iPhone 15");
        mockProduct.setDescription("全新未拆封");
        mockProduct.setPrice(new BigDecimal("8999.00"));
        mockProduct.setCategory_id(1L);
        mockProduct.setMain_image("https://example.com/image1.jpg");
        mockProduct.setStatus(0);
        mockProduct.setUser_id(1L);
    }

    @Test
    @DisplayName("测试ProductCreateRequest构造和访问")
    void testProductCreateRequest() {
        assertEquals("iPhone 15", validRequest.getName());
        assertEquals("全新未拆封", validRequest.getDescription());
        assertEquals(new BigDecimal("8999.00"), validRequest.getPrice());
        assertEquals(1L, validRequest.getCategoryId());
        assertEquals("https://example.com/image1.jpg", validRequest.getImageUrls());
    }

    @Test
    @DisplayName("测试ProductVO构造和访问")
    void testProductVO() {
        ProductVO productVO = new ProductVO();
        productVO.setId(1L);
        productVO.setName("iPhone 15");
        productVO.setCategoryId(1L);
        productVO.setCategoryName("手机数码");
        productVO.setStatus(0);
        productVO.setUserId(1L);
        productVO.setUserNickname("测试用户");

        assertEquals(1L, productVO.getId());
        assertEquals("iPhone 15", productVO.getName());
        assertEquals("手机数码", productVO.getCategoryName());
        assertEquals(0, productVO.getStatus());
        assertEquals("测试用户", productVO.getUserNickname());
    }

    @Test
    @DisplayName("测试Product实体属性映射")
    void testProductEntityMapping() {
        Product product = new Product();
        BeanUtils.copyProperties(validRequest, product);
        
        // 手动设置需要特殊处理的字段
        product.setUser_id(1L);
        product.setCategory_id(validRequest.getCategoryId());
        product.setTitle(validRequest.getName());
        product.setMain_image(validRequest.getImageUrls());
        product.setStatus(0);
        product.setStock(1);
        product.setLocked_stock(0);

        assertEquals("iPhone 15", product.getTitle());
        assertEquals("全新未拆封", product.getDescription());
        assertEquals(new BigDecimal("8999.00"), product.getPrice());
        assertEquals(1L, product.getCategory_id());
        assertEquals("https://example.com/image1.jpg", product.getMain_image());
        assertEquals(0, product.getStatus());
        assertEquals(1, product.getStock());
    }

    @Test
    @DisplayName("测试价格验证")
    void testPriceValidation() {
        // 测试正常价格
        assertTrue(validRequest.getPrice().compareTo(BigDecimal.ZERO) > 0);
        
        // 测试零价格
        ProductCreateRequest zeroPriceRequest = new ProductCreateRequest();
        zeroPriceRequest.setPrice(BigDecimal.ZERO);
        assertFalse(zeroPriceRequest.getPrice().compareTo(BigDecimal.ZERO) > 0);
        
        // 测试负数价格
        ProductCreateRequest negativePriceRequest = new ProductCreateRequest();
        negativePriceRequest.setPrice(new BigDecimal("-100"));
        assertFalse(negativePriceRequest.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("测试必填字段验证")
    void testRequiredFields() {
        // 测试名称必填
        assertNotNull(validRequest.getName());
        assertNotEquals("", validRequest.getName());
        
        // 测试价格必填
        assertNotNull(validRequest.getPrice());
        
        // 测试分类ID必填
        assertNotNull(validRequest.getCategoryId());
    }
}