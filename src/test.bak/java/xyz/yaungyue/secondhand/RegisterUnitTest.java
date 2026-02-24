package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;

import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.mapper.UserMapper;
import xyz.yaungyue.secondhand.model.dto.request.RegisterRequest;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.service.impl.UserServiceImpl;
import xyz.yaungyue.secondhand.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("注册功能单元测试")
class RegisterUnitTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

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
        testUser.setPassword("$2a$10$encryptedPassword");
        testUser.setNickname("测试用户");
        testUser.setPhone("13812345678");
        testUser.setCredit_score(100);
        testUser.setStatus(1);
        testUser.setCreate_time(LocalDateTime.now());
        testUser.setUpdate_time(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试正常注册流程")
    void testNormalRegistration() {
        // 准备mock数据
        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenReturn(true);

        // 执行测试
        User result = userServiceImpl.register(validRegisterRequest);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser123");
        assertThat(result.getNickname()).isEqualTo("测试用户");
        assertThat(result.getPhone()).isEqualTo("13812345678");
        assertThat(result.getCredit_score()).isEqualTo(100);
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getPassword()).isNull(); // 密码不应该返回

        // 验证方法调用
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试密码不匹配")
    void testPasswordMismatch() {
        RegisterRequest mismatchRequest = new RegisterRequest(
            "testuser123",
            "password123",
            "differentpassword",
            "测试用户",
            "13812345678"
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.register(mismatchRequest);
        });

        assertEquals(ErrorCode.USER_PASSWORD_MISMATCH.getCode(), exception.getCode());
        assertEquals(ErrorCode.USER_PASSWORD_MISMATCH.getMessage(), exception.getMessage());
        verify(userServiceImpl, never()).count(any(QueryWrapper.class));
        verify(userServiceImpl, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试用户名已存在")
    void testUsernameExists() {
        // 模拟用户名已存在
        when(userServiceImpl.count(argThat(wrapper -> {
            if (wrapper instanceof QueryWrapper) {
                QueryWrapper<User> qw = (QueryWrapper<User>) wrapper;
                return qw.getSqlSegment().contains("username = ?");
            }
            return false;
        }))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.register(validRegisterRequest);
        });

        assertEquals(ErrorCode.USER_USERNAME_EXISTS.getCode(), exception.getCode());
        assertEquals(ErrorCode.USER_USERNAME_EXISTS.getMessage(), exception.getMessage());
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(userServiceImpl, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试手机号已存在")
    void testPhoneExists() {
        // 第一次查询用户名不存在，第二次查询手机号存在
        when(userServiceImpl.count(argThat(wrapper -> {
            if (wrapper instanceof QueryWrapper) {
                QueryWrapper<User> qw = (QueryWrapper<User>) wrapper;
                return qw.getSqlSegment().contains("username = ?");
            }
            return false;
        }))).thenReturn(0L);
        
        when(userServiceImpl.count(argThat(wrapper -> {
            if (wrapper instanceof QueryWrapper) {
                QueryWrapper<User> qw = (QueryWrapper<User>) wrapper;
                return qw.getSqlSegment().contains("phone = ?");
            }
            return false;
        }))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.register(validRegisterRequest);
        });

        assertEquals(ErrorCode.USER_PHONE_EXISTS.getCode(), exception.getCode());
        assertEquals(ErrorCode.USER_PHONE_EXISTS.getMessage(), exception.getMessage());
        verify(userServiceImpl, times(2)).count(any(QueryWrapper.class));
        verify(userServiceImpl, never()).save(any(User.class));
    }

    @Test
    @DisplayName("测试数据库保存失败")
    void testSaveFailed() {
        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userServiceImpl.register(validRegisterRequest);
        });

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), exception.getCode());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), exception.getMessage());
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
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
        when(userServiceImpl.save(any(User.class))).thenReturn(true);

        User result = userServiceImpl.register(requestWithoutPhone);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("nophoneuser");
        assertThat(result.getNickname()).isEqualTo("无手机用户");
        assertThat(result.getPhone()).isNull();
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试空手机号字符串的注册")
    void testRegisterWithEmptyPhone() {
        RegisterRequest requestWithEmptyPhone = new RegisterRequest(
            "emptyphoneuser",
            "password123",
            "password123",
            "空手机用户",
            ""
        );

        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenReturn(true);

        User result = userServiceImpl.register(requestWithEmptyPhone);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("emptyphoneuser");
        assertThat(result.getNickname()).isEqualTo("空手机用户");
        assertThat(result.getPhone()).isEqualTo("");
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试特殊字符用户名注册")
    void testSpecialCharacterUsername() {
        RegisterRequest specialUsernameRequest = new RegisterRequest(
            "user_123_test",
            "password123",
            "password123",
            "特殊字符用户",
            "13812345678"
        );

        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenReturn(true);

        User result = userServiceImpl.register(specialUsernameRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("user_123_test");
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试中文昵称注册")
    void testChineseNicknameRegistration() {
        RegisterRequest chineseNicknameRequest = new RegisterRequest(
            "chinesetest",
            "password123",
            "password123",
            "张三李四王五赵六",
            "13812345678"
        );

        when(userServiceImpl.count(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userServiceImpl.save(any(User.class))).thenReturn(true);

        User result = userServiceImpl.register(chineseNicknameRequest);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("张三李四王五赵六");
        verify(userServiceImpl).count(any(QueryWrapper.class));
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试长昵称注册（边界值测试）")
    void testLongNicknameRegistration() {
        String longNickname = "这是一个很长的昵称测试字符串用来测试昵称长度限制的边界情况";
        RegisterRequest longNicknameRequest = new RegisterRequest(
            "longnicknametest",
            "password123",
            "password123",
            longNickname,
            "13812345678"
        );

        doReturn(0L).when(userServiceImpl).count(any());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        doReturn(true).when(userServiceImpl).save(any(User.class));

        User result = userServiceImpl.register(longNicknameRequest);

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(longNickname);
        verify(userServiceImpl).count(any());
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试用户对象属性设置")
    void testUserObjectProperties() {
        doReturn(0L).when(userServiceImpl).count(any());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        doReturn(true).when(userServiceImpl).save(any(User.class));

        User result = userServiceImpl.register(validRegisterRequest);

        assertThat(result).isNotNull();
        // 验证默认值设置
        assertThat(result.getCredit_score()).isEqualTo(100);
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getCreate_time()).isNotNull();
        assertThat(result.getUpdate_time()).isNotNull();
        assertThat(result.getAvatar()).isNull();
        
        // 验证时间设置
        assertThat(result.getCreate_time()).isEqualTo(result.getUpdate_time());
        verify(userServiceImpl).count(any());
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(any(User.class));
    }

    @Test
    @DisplayName("测试密码加密")
    void testPasswordEncryption() {
        doReturn(0L).when(userServiceImpl).count(any());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encryptedHash123456");
        doReturn(true).when(userServiceImpl).save(any(User.class));

        User result = userServiceImpl.register(validRegisterRequest);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).save(argThat(user -> 
            "$2a$10$encryptedHash123456".equals(user.getPassword())
        ));
    }

    @Test
    @DisplayName("测试多次连续注册")
    void testMultipleRegistrations() {
        RegisterRequest[] requests = {
            new RegisterRequest("user1", "pass123", "pass123", "用户1", "13800000001", "user1@example.com"),
            new RegisterRequest("user2", "pass123", "pass123", "用户2", "13800000002", "user2@example.com"),
            new RegisterRequest("user3", "pass123", "pass123", "用户3", "13800000003", "user3@example.com")
        };

        for (int i = 0; i < requests.length; i++) {
            final int index = i;
            doReturn(0L).when(userServiceImpl).count(any());
            when(passwordEncoder.encode("pass123")).thenReturn("$2a$10$encodedPass" + index);
            doReturn(true).when(userServiceImpl).save(any(User.class));

            User result = userServiceImpl.register(requests[i]);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user" + (index + 1));
            verify(userServiceImpl, times(index + 1)).count(any());
        }
    }
}