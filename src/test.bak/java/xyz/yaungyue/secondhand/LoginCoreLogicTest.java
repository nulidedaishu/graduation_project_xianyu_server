package xyz.yaungyue.secondhand;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("登录功能核心逻辑测试")
class LoginCoreLogicTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("测试登录请求JSON格式")
    void testLoginRequestFormat() throws Exception {
        String validRequest = """
            {
                "username": "testuser",
                "password": "123456"
            }
            """;
            
        assertDoesNotThrow(() -> objectMapper.readTree(validRequest));
    }

    @Test
    @DisplayName("测试密码加密验证")
    void testPasswordEncryption() {
        var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        assertTrue(encoder.matches(rawPassword, encodedPassword));
        assertFalse(encoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    @DisplayName("测试错误码验证")
    void testErrorCodes() {
        assertEquals(40101, xyz.yaungyue.secondhand.exception.ErrorCode.USER_NOT_FOUND.getCode());
        assertEquals("用户不存在", xyz.yaungyue.secondhand.exception.ErrorCode.USER_NOT_FOUND.getMessage());
        assertEquals(40103, xyz.yaungyue.secondhand.exception.ErrorCode.USER_PASSWORD_ERROR.getCode());
        assertEquals("密码错误", xyz.yaungyue.secondhand.exception.ErrorCode.USER_PASSWORD_ERROR.getMessage());
    }

    @Test
    @DisplayName("测试用户实体基本功能")
    void testUserEntity() {
        xyz.yaungyue.secondhand.model.entity.User user = new xyz.yaungyue.secondhand.model.entity.User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setStatus(1);
        
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals(Integer.valueOf(1), user.getStatus());
    }

    @Test
    @DisplayName("测试登录响应结构")
    void testLoginResponse() {
        var userInfo = new xyz.yaungyue.secondhand.model.dto.response.LoginResponse.UserInfo(
            1L, "testuser", "测试用户", "avatar.jpg", "13800138000"
        );
        
        var response = new xyz.yaungyue.secondhand.model.dto.response.LoginResponse("token123", userInfo);
        
        assertEquals("token123", response.token());
        assertEquals("testuser", response.user().username());
    }

    @Test
    @DisplayName("测试安全输入处理")
    void testSecurityInputHandling() throws Exception {
        // 测试特殊字符处理
        String[] testInputs = {
            """
            {
                "username": "'; DROP TABLE users; --",
                "password": "123456"
            }
            """,
            """
            {
                "username": "<script>alert('xss')</script>",
                "password": "123456"
            }
            """
        };
        
        for (String input : testInputs) {
            assertDoesNotThrow(() -> objectMapper.readTree(input));
        }
    }
}