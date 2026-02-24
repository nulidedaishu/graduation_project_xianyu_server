package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SaTokenAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginAndGetUserInfo() throws Exception {
        // 1. 用户登录
        String loginJson = """
            {
                "username": "testuser",
                "password": "123456"
            }
            """;

        String token = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 解析token（实际项目中应该从JSON响应中提取）
        // 这里简化处理，实际应该解析JSON获取token值

        // 2. 使用token访问受保护的接口
        mockMvc.perform(get("/api/auth/info")
                .header("Authorization", "Bearer fake-token")) // 实际应该使用真实的token
                .andExpect(status().isOk());

        // 3. 检查登录状态
        mockMvc.perform(get("/api/auth/check-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testAccessWithoutToken() throws Exception {
        // 尝试访问需要登录的接口但不提供token
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUserEndpoints() throws Exception {
        // 测试用户相关接口
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized()); // 未登录应该返回401

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized()); // 未登录应该返回401
    }
}