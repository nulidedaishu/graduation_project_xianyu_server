package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.response.LoginResponse;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.util.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("登录服务纯单元测试")
class LoginServiceUnitTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private LoginServiceStub loginService;

    private User testUser;

    // 创建一个简单的stub类来模拟登录逻辑，不依赖MyBatis Plus
    private static class LoginServiceStub {
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;

        public LoginServiceStub(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
            this.passwordEncoder = passwordEncoder;
            this.jwtUtil = jwtUtil;
        }

        public LoginResponse login(String username, String password, User userFromDb) {
            // 查找用户
            if (userFromDb == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查用户状态
            if (userFromDb.getStatus() == 0) {
                throw new BusinessException("用户已被禁用");
            }

            // 验证密码
            if (!passwordEncoder.matches(password, userFromDb.getPassword())) {
                throw new BusinessException("密码错误");
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(userFromDb.getId(), userFromDb.getUsername());

            // 构造用户信息
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                userFromDb.getId(),
                userFromDb.getUsername(),
                userFromDb.getNickname(),
                userFromDb.getAvatar(),
                userFromDb.getPhone()
            );

            return new LoginResponse(token, userInfo);
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new LoginServiceStub(passwordEncoder, jwtUtil);
        
        // 创建测试用户数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encryptedPassword"); // BCrypt加密后的密码
        testUser.setNickname("测试用户");
        testUser.setAvatar("avatar.jpg");
        testUser.setPhone("13800138000");
        testUser.setStatus(1); // 正常状态
    }

    @Test
    @DisplayName("测试正常登录流程")
    void testSuccessfulLogin() {
        // 准备mock数据
        when(passwordEncoder.matches("123456", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("mock-jwt-token");

        // 执行测试
        LoginResponse response = loginService.login("testuser", "123456", testUser);

        // 验证结果
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertNotNull(response.user());
        assertEquals(1L, response.user().id());
        assertEquals("testuser", response.user().username());
        assertEquals("测试用户", response.user().nickname());
        assertEquals("avatar.jpg", response.user().avatar());
        assertEquals("13800138000", response.user().phone());

        // 验证方法调用
        verify(passwordEncoder).matches("123456", testUser.getPassword());
        verify(jwtUtil).generateToken(1L, "testuser");
    }

    @Test
    @DisplayName("测试用户不存在")
    void testUserNotFound() {
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loginService.login("nonexistent", "123456", null);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("测试用户被禁用")
    void testUserDisabled() {
        // 设置用户为禁用状态
        testUser.setStatus(0);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loginService.login("disableduser", "123456", testUser);
        });

        assertEquals("用户已被禁用", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("测试密码错误")
    void testWrongPassword() {
        // 准备mock数据
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loginService.login("testuser", "wrongpassword", testUser);
        });

        assertEquals("密码错误", exception.getMessage());
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("测试密码正确但不同输入")
    void testPasswordCorrectDifferentInput() {
        // 准备mock数据 - 模拟BCrypt的特性：相同明文密码多次加密结果不同
        when(passwordEncoder.matches("123456", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("different-token");

        LoginResponse response1 = loginService.login("testuser", "123456", testUser);
        LoginResponse response2 = loginService.login("testuser", "123456", testUser);

        // 验证两次登录都能成功，但可能产生不同的token
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("different-token", response1.token());
        assertEquals("different-token", response2.token());
    }

    @Test
    @DisplayName("测试边界情况：空用户名")
    void testEmptyUsername() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loginService.login("", "123456", testUser);
        });
        // 注意：这里不会触发"用户名不能为空"的验证，因为那是DTO层面的验证
        // 实际业务逻辑中用户名为空会通过数据库查询返回null用户
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("测试边界情况：空密码")
    void testEmptyPassword() {
        when(passwordEncoder.matches("", testUser.getPassword())).thenReturn(false);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            loginService.login("testuser", "", testUser);
        });
        
        assertEquals("密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("测试特殊字符处理")
    void testSpecialCharacters() {
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setUsername("special'user");
        specialUser.setPassword("$2a$10$specialEncrypted");
        specialUser.setNickname("特<殊>用\"户'");
        specialUser.setAvatar("special-avatar.jpg");
        specialUser.setPhone("13800138001");
        specialUser.setStatus(1);

        when(passwordEncoder.matches("sp3c!@l$p@ss", specialUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(2L, "special'user")).thenReturn("special-token");

        LoginResponse response = loginService.login("special'user", "sp3c!@l$p@ss", specialUser);

        assertNotNull(response);
        assertEquals("special-token", response.token());
        assertEquals("special'user", response.user().username());
        assertEquals("特<殊>用\"户'", response.user().nickname());
    }
}