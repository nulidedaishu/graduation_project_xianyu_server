package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.response.LoginResponse;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.service.impl.UserServiceImpl;
import xyz.yaungyue.secondhand.util.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("登录核心逻辑单元测试")
class LoginCoreLogicUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
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
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("123456", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("mock-jwt-token");

        // 执行测试
        LoginResponse response = userServiceImpl.login("testuser", "123456");

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
        verify(userService).findByUsername("testuser");
        verify(passwordEncoder).matches("123456", testUser.getPassword());
        verify(jwtUtil).generateToken(1L, "testuser");
    }

    @Test
    @DisplayName("测试用户不存在")
    void testUserNotFound() {
        // 准备mock数据
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.login("nonexistent", "123456");
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userService).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("测试用户被禁用")
    void testUserDisabled() {
        // 设置用户为禁用状态
        testUser.setStatus(0);
        when(userService.findByUsername("disableduser")).thenReturn(testUser);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.login("disableduser", "123456");
        });

        assertEquals("用户已被禁用", exception.getMessage());
        verify(userService).findByUsername("disableduser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("测试密码错误")
    void testWrongPassword() {
        // 准备mock数据
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.login("testuser", "wrongpassword");
        });

        assertEquals("密码错误", exception.getMessage());
        verify(userService).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("测试findByUsername方法")
    void testFindByUsername() {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        User result = userServiceImpl.findByUsername("testuser");

        assertEquals(testUser, result);
        verify(userService).findByUsername("testuser");
    }

    @Test
    @DisplayName("测试findById方法")
    void testFindById() {
        when(userService.findById(1L)).thenReturn(testUser);

        User result = userServiceImpl.findById(1L);

        assertEquals(testUser, result);
        verify(userService).findById(1L);
    }

    @Test
    @DisplayName("测试existsByUsername方法")
    void testExistsByUsername() {
        when(userService.existsByUsername("testuser")).thenReturn(true);
        when(userService.existsByUsername("nonexistent")).thenReturn(false);

        assertTrue(userServiceImpl.existsByUsername("testuser"));
        assertFalse(userServiceImpl.existsByUsername("nonexistent"));

        verify(userService).existsByUsername("testuser");
        verify(userService).existsByUsername("nonexistent");
    }
}