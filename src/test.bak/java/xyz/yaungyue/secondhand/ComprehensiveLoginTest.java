package xyz.yaungyue.secondhand;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("综合登录功能测试")
class ComprehensiveLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "123456";
    private static String authToken;

    @Test
    @Order(1)
    @DisplayName("测试正常登录流程")
    void testSuccessfulLogin() throws Exception {
        String loginJson = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.data.user.username").value(TEST_USERNAME))
                .andReturn();

        // 提取token用于后续测试
        String responseContent = result.getResponse().getContentAsString();
        authToken = extractTokenFromResponse(responseContent);
        
        Assertions.assertNotNull(authToken, "登录成功后应该返回有效的token");
        System.out.println("登录成功，获得token: " + authToken);
    }

    @Test
    @Order(2)
    @DisplayName("测试用户名不存在")
    void testLoginWithNonExistentUser() throws Exception {
        String loginJson = createLoginRequest("nonexistent_user", "any_password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.message").value("用户不存在"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("测试密码错误")
    void testLoginWithWrongPassword() throws Exception {
        String loginJson = createLoginRequest(TEST_USERNAME, "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40103))
                .andExpect(jsonPath("$.message").value("密码错误"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @Order(4)
    @DisplayName("测试用户名为空")
    void testLoginWithEmptyUsername() throws Exception {
        String loginJson = createLoginRequest("", "123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk()) // 业务异常返回200，但code不是200
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名不能为空"))
                .andReturn();

        System.out.println("用户名为空的响应: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(5)
    @DisplayName("测试密码为空")
    void testLoginWithEmptyPassword() throws Exception {
        String loginJson = createLoginRequest("testuser", "");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码不能为空"))
                .andReturn();

        System.out.println("密码为空的响应: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(6)
    @DisplayName("测试缺少必要字段")
    void testLoginWithMissingFields() throws Exception {
        String incompleteJson = """
            {
                "username": "testuser"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        System.out.println("缺少字段的响应: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(7)
    @DisplayName("测试登录后获取用户信息")
    void testGetUserInfoAfterLogin() throws Exception {
        // 确保已有token
        if (authToken == null) {
            testSuccessfulLogin();
        }

        mockMvc.perform(get("/api/auth/info")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
    }

    @Test
    @Order(8)
    @DisplayName("测试未登录状态下检查登录状态")
    void testCheckLoginStatusWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/check-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @Order(9)
    @DisplayName("测试登录后检查登录状态")
    void testCheckLoginStatusWhenLoggedIn() throws Exception {
        // 确保已登录
        if (authToken == null) {
            testSuccessfulLogin();
        }

        mockMvc.perform(get("/api/auth/check-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @Order(10)
    @DisplayName("测试登录后登出")
    void testLogout() throws Exception {
        // 确保已登录
        if (authToken == null) {
            testSuccessfulLogin();
        }

        // 登出
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证登出后无法访问受保护资源
        mockMvc.perform(get("/api/auth/info")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户未登录"));
    }

    @Test
    @Order(11)
    @DisplayName("测试无效的JSON格式")
    void testLoginWithInvalidJsonFormat() throws Exception {
        String invalidJson = "{ invalid json format }";

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        System.out.println("无效JSON格式的响应: " + result.getResponse().getContentAsString());
    }

    @Test
    @Order(12)
    @DisplayName("测试SQL注入攻击防护")
    void testSqlInjectionProtection() throws Exception {
        String maliciousUsername = "'; DROP TABLE users; --";
        String loginJson = createLoginRequest(maliciousUsername, "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101)) // 应该返回用户不存在而不是执行SQL
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @Order(13)
    @DisplayName("测试XSS攻击防护")
    void testXssProtection() throws Exception {
        String xssUsername = "<script>alert('xss')</script>";
        String loginJson = createLoginRequest(xssUsername, "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101)) // 应该正确处理特殊字符
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    // 辅助方法
    private String createLoginRequest(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginRequestDto(username, password));
    }

    private String extractTokenFromResponse(String jsonResponse) throws Exception {
        // 解析JSON提取token
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            return jsonNode.path("data").path("token").asText();
        } catch (Exception e) {
            System.err.println("解析token失败: " + e.getMessage());
            return null;
        }
    }

    // 内部DTO类用于测试
    private record LoginRequestDto(String username, String password) {}
}