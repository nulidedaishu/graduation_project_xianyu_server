package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginSuccess() throws Exception {
        // 测试登录成功的情况
        String loginJson = """
            {
                "username": "testuser",
                "password": "123456"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user").exists());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // 测试登录凭据无效的情况
        String loginJson = """
            {
                "username": "nonexistent",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk()) // 业务异常也会返回200，但code不是200
                .andExpect(jsonPath("$.code").value(40101)); // USER_NOT_FOUND
    }

    @Test
    void testLoginWithMissingFields() throws Exception {
        // 测试缺少必填字段的情况
        String loginJson = """
            {
                "username": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isBadRequest());
    }
}