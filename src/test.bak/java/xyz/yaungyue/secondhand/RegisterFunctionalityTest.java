package xyz.yaungyue.secondhand;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.model.dto.request.RegisterRequest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 测试完成后回滚事务
public class RegisterFunctionalityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(
            "testuser123",
            "password123",
            "password123",
            "测试用户",
            "13812345678"
        );
    }

    @Test
    void testRegisterSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser123"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.phone").value("13812345678"))
                .andExpect(jsonPath("$.data.password").doesNotExist()); // 密码不应返回
    }

    @Test
    void testRegisterWithExistingUsername() throws Exception {
        // 先注册一个用户
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)));

        // 再次使用相同用户名注册，应该失败
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40106))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void testRegisterWithExistingPhone() throws Exception {
        // 先注册一个用户
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)));

        // 使用不同用户名但相同手机号注册，应该失败
        RegisterRequest requestWithSamePhone = new RegisterRequest(
            "differentuser",
            "password123",
            "password123",
            "另一个用户",
            "13812345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithSamePhone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40104))
                .andExpect(jsonPath("$.message").value("手机号已存在"));
    }

    @Test
    void testRegisterWithPasswordMismatch() throws Exception {
        RegisterRequest requestWithMismatchPassword = new RegisterRequest(
            "testuser123",
            "password123",
            "differentpassword",
            "测试用户",
            "13812345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithMismatchPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40107))
                .andExpect(jsonPath("$.message").value("两次输入的密码不一致"));
    }

    @Test
    void testRegisterWithInvalidUsername() throws Exception {
        // 用户名太短
        RegisterRequest shortUsernameRequest = new RegisterRequest(
            "ab",
            "password123",
            "password123",
            "测试用户",
            "13812345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortUsernameRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("用户名长度必须在3-20个字符之间")));

        // 用户名包含特殊字符
        RegisterRequest invalidCharRequest = new RegisterRequest(
            "test-user!",
            "password123",
            "password123",
            "测试用户",
            "13812345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCharRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("用户名只能包含字母、数字和下划线")));
    }

    @Test
    void testRegisterWithInvalidPhone() throws Exception {
        RegisterRequest invalidPhoneRequest = new RegisterRequest(
            "testuser123",
            "password123",
            "password123",
            "测试用户",
            "12345678901" // 无效的手机号格式
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPhoneRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("手机号格式不正确")));
    }

    @Test
    void testRegisterWithoutRequiredFields() throws Exception {
        // 缺少用户名
        RegisterRequest noUsernameRequest = new RegisterRequest(
            "",
            "password123",
            "password123",
            "测试用户",
            "13812345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noUsernameRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("用户名不能为空")));

        // 缺少密码
        RegisterRequest noPasswordRequest = new RegisterRequest(
            "testuser123",
            "",
            "password123",
            "测试用户",
            "13812345678"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("密码不能为空")));
    }

    @Test
    void testRegisterWithMinimalRequiredFields() throws Exception {
        // 只提供必需字段（用户名、密码、确认密码、昵称），不提供手机号
        RegisterRequest minimalRequest = new RegisterRequest(
            "minimaluser",
            "password123",
            "password123",
            "最小化用户",
            null
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("minimaluser"))
                .andExpect(jsonPath("$.data.nickname").value("最小化用户"))
                .andExpect(jsonPath("$.data.phone").isEmpty()); // 手机号应为空
    }
}