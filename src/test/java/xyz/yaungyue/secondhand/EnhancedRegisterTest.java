package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import xyz.yaungyue.secondhand.constant.SystemConstants;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.mapper.UserMapper;
import xyz.yaungyue.secondhand.model.dto.request.RegisterRequest;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.model.entity.UserRole;
import xyz.yaungyue.secondhand.service.UserRoleService;
import xyz.yaungyue.secondhand.service.impl.UserServiceImpl;
import xyz.yaungyue.secondhand.util.JwtUtil;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("注册功能增强测试")
class EnhancedRegisterTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private RegisterRequest validRegisterRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(
            "testuser123",
            "password123",
            "password123",
            "测试用户",
            "13812345678"
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser123");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setPhone("13812345678");
        testUser.setCredit_score(SystemConstants.DEFAULT_CREDIT_SCORE);
        testUser.setStatus(SystemConstants.USER_STATUS_NORMAL);
        testUser.setCreate_time(LocalDateTime.now());
        testUser.setUpdate_time(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试成功注册并分配默认角色")
    void testRegisterSuccessWithDefaultRole() {
        // 模拟数据库查询结果
        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // 设置用户ID
            return true;
        });
        when(userRoleService.save(any(UserRole.class))).thenReturn(true);

        // 执行注册
        User registeredUser = userServiceImpl.register(validRegisterRequest);

        // 验证结果
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isEqualTo(1L);
        assertThat(registeredUser.getUsername()).isEqualTo("testuser123");
        assertThat(registeredUser.getNickname()).isEqualTo("测试用户");
        assertThat(registeredUser.getPhone()).isEqualTo("13812345678");
        assertThat(registeredUser.getCredit_score()).isEqualTo(SystemConstants.DEFAULT_CREDIT_SCORE);
        assertThat(registeredUser.getStatus()).isEqualTo(SystemConstants.USER_STATUS_NORMAL);
        assertThat(registeredUser.getPassword()).isNull(); // 密码不应返回

        // 验证方法调用
        verify(userServiceImpl).count(any(QueryWrapper.class)); // 用户名检查
        verify(userServiceImpl).count(any(QueryWrapper.class)); // 手机号检查
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
        verify(userRoleService).save(any(UserRole.class));

        // 验证UserRole对象的属性
        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleService).save(userRoleCaptor.capture());
        UserRole capturedUserRole = userRoleCaptor.getValue();
        assertThat(capturedUserRole.getUser_id()).isEqualTo(1L);
        assertThat(capturedUserRole.getRole_id()).isEqualTo(SystemConstants.ROLE_USER);
    }

    @Test
    @DisplayName("测试角色分配失败")
    void testRoleAssignmentFailed() {
        // 模拟数据库查询结果
        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return true;
        });
        when(userRoleService.save(any(UserRole.class))).thenReturn(false);

        // 执行注册，应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.register(validRegisterRequest);
        });

        assertThat(exception.getCode()).isEqualTo(500);
        assertThat(exception.getMessage()).isEqualTo("服务器内部错误");

        // 验证用户被保存但角色分配失败
        verify(userServiceImpl).save(any(User.class));
        verify(userRoleService).save(any(UserRole.class));
    }

    @Test
    @DisplayName("测试不提供手机号的注册")
    void testRegisterWithoutPhone() {
        RegisterRequest requestWithoutPhone = new RegisterRequest(
            "nophoneuser",
            "password123",
            "password123",
            "无手机用户",
            null
        );

        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return true;
        });
        when(userRoleService.save(any(UserRole.class))).thenReturn(true);

        User registeredUser = userServiceImpl.register(requestWithoutPhone);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getId()).isEqualTo(2L);
        assertThat(registeredUser.getUsername()).isEqualTo("nophoneuser");
        assertThat(registeredUser.getPhone()).isNull();

        verify(userRoleService).save(any(UserRole.class));
    }

    @Test
    @DisplayName("测试系统常量值")
    void testSystemConstants() {
        assertThat(SystemConstants.ROLE_ADMIN).isEqualTo(1L);
        assertThat(SystemConstants.ROLE_USER).isEqualTo(2L);
        assertThat(SystemConstants.USER_STATUS_NORMAL).isEqualTo(1);
        assertThat(SystemConstants.USER_STATUS_DISABLED).isEqualTo(0);
        assertThat(SystemConstants.DEFAULT_CREDIT_SCORE).isEqualTo(100);
    }
}