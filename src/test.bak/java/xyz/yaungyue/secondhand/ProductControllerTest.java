package xyz.yaungyue.secondhand;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("商品控制器测试")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private SaTokenUtil saTokenUtil;

    private ProductCreateRequest validRequest;
    private ProductVO mockProductVO;

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

        mockProductVO = new ProductVO();
        mockProductVO.setId(1L);
        mockProductVO.setName("iPhone 15");
        mockProductVO.setDescription("全新未拆封");
        mockProductVO.setPrice(new BigDecimal("8999.00"));
        mockProductVO.setCategoryId(1L);
        mockProductVO.setCategoryName("手机数码");
        mockProductVO.setStatus(0);
        mockProductVO.setUserId(1L);
        mockProductVO.setUserNickname("测试用户");
    }

    @Test
    @DisplayName("测试商品发布成功")
    void testCreateProductSuccess() throws Exception {
        // 模拟登录用户
        when(saTokenUtil.getLoginId()).thenReturn(1L);
        
        // 模拟服务层返回
        when(productService.createProduct(any(ProductCreateRequest.class), eq(1L)))
            .thenReturn(mockProductVO);

        String requestBody = objectMapper.writeValueAsString(validRequest);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("商品发布成功，等待审核"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("iPhone 15"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("测试商品名称为空")
    void testProductNameEmpty() throws Exception {
        validRequest.setName("");

        String requestBody = objectMapper.writeValueAsString(validRequest);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("测试商品价格为空")
    void testProductPriceNull() throws Exception {
        validRequest.setPrice(null);

        String requestBody = objectMapper.writeValueAsString(validRequest);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("测试商品价格为负数")
    void testProductPriceNegative() throws Exception {
        validRequest.setPrice(new BigDecimal("-100"));

        String requestBody = objectMapper.writeValueAsString(validRequest);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("测试分类ID为空")
    void testCategoryIdNull() throws Exception {
        validRequest.setCategoryId(null);

        String requestBody = objectMapper.writeValueAsString(validRequest);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}