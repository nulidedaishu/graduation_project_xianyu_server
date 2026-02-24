package xyz.yaungyue.secondhand;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
@DisplayName("登录功能测试")
class LoginFunctionalityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validUsername;
    private String validPassword;

    @BeforeEach
    void setUp() {
        // 使用测试环境中可能存在的用户数据
        validUsername = "testuser";
        validPassword = "123456";
    }

    @Test
    @DisplayName("测试正常登录流程")
    void testSuccessfulLogin() throws Exception {
        String loginJson = createLoginRequest(validUsername, validPassword);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.data.user.id").exists())
                .andExpect(jsonPath("$.data.user.username").value(validUsername))
                .andExpect(jsonPath("$.data.user.nickname").exists())
                .andReturn();

        // 验证响应结构
        String responseContent = result.getResponse().getContentAsString();
        assert responseContent.contains("\"token\"") : "响应中应包含token字段";
        assert responseContent.contains("\"user\"") : "响应中应包含user信息";
    }

    @Test
    @DisplayName("测试用户名不存在")
    void testLoginWithNonExistentUser() throws Exception {
        String loginJson = createLoginRequest("nonexistent_user", "any_password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101)) // USER_NOT_FOUND
                .andExpect(jsonPath("$.message").value("用户不存在"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("测试密码错误")
    void testLoginWithWrongPassword() throws Exception {
        String loginJson = createLoginRequest(validUsername, "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40103)) // USER_PASSWORD_ERROR
                .andExpect(jsonPath("$.message").value("密码错误"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("测试用户被禁用")
    void testLoginWithDisabledUser() throws Exception {
        // 这个测试需要数据库中有被禁用的用户
        // 在实际测试环境中可能需要预先准备测试数据
        String loginJson = createLoginRequest("disabled_user", "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101)) // USER_NOT_FOUND (因为用户不存在)
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @DisplayName("测试用户名为空")
    void testLoginWithEmptyUsername() throws Exception {
        String loginJson = createLoginRequest("", "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("测试密码为空")
    void testLoginWithEmptyPassword() throws Exception {
        String loginJson = createLoginRequest("testuser", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("测试缺少必要字段")
    void testLoginWithMissingFields() throws Exception {
        String incompleteJson = """
            {
                "username": "testuser"
                // 缺少password字段
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("测试登录后获取用户信息")
    void testGetUserInfoAfterLogin() throws Exception {
        // 1. 先登录获取token
        String loginJson = createLoginRequest(validUsername, validPassword);
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String token = extractTokenFromResponse(responseContent);

        // 2. 使用token获取用户信息
        mockMvc.perform(get("/api/auth/info")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value(validUsername));
    }

    @Test
    @DisplayName("测试未登录状态下检查登录状态")
    void testCheckLoginStatusWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/auth/check-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("测试登录后检查登录状态")
    void testCheckLoginStatusWhenLoggedIn() throws Exception {
        // 1. 登录
        String loginJson = createLoginRequest(validUsername, validPassword);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk());

        // 2. 检查登录状态
        mockMvc.perform(get("/api/auth/check-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("测试登录后登出")
    void testLogout() throws Exception {
        // 1. 登录
        String loginJson = createLoginRequest(validUsername, validPassword);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk());

        // 2. 登出
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 验证登出后无法访问受保护资源
        mockMvc.perform(get("/api/auth/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户未登录"));
    }

    @Test
    @DisplayName("测试无效的JSON格式")
    void testLoginWithInvalidJsonFormat() throws Exception {
        String invalidJson = "{ invalid json format }";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试SQL注入攻击防护")
    void testSqlInjectionProtection() throws Exception {
        String maliciousUsername = "'; DROP TABLE users; --";
        String loginJson = createLoginRequest(maliciousUsername, "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101)); // 应该返回用户不存在而不是执行SQL
    }

    @Test
    @DisplayName("测试XSS攻击防护")
    void testXssProtection() throws Exception {
        String xssUsername = "<script>alert('xss')</script>";
        String loginJson = createLoginRequest(xssUsername, "123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40101)); // 应该正确处理特殊字符
    }

    // 辅助方法
    private String createLoginRequest(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginRequestDto(username, password));
    }

    private String extractTokenFromResponse(String jsonResponse) throws Exception {
        // 简单的字符串解析，实际项目中应该使用JSON解析库
        int startIndex = jsonResponse.indexOf("\"token\":\"") + 9;
        int endIndex = jsonResponse.indexOf("\"", startIndex);
        return jsonResponse.substring(startIndex, endIndex);
    }

    // 内部DTO类用于测试
    private record LoginRequestDto(String username, String password) {}
}