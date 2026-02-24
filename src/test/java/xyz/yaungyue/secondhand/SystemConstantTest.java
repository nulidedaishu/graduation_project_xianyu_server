package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import xyz.yaungyue.secondhand.constant.SystemConstants;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SystemConstantTest {

    @Test
    void testRoleConstants() {
        // 测试角色常量
        assertThat(SystemConstants.ROLE_ADMIN).isEqualTo(1L);
        assertThat(SystemConstants.ROLE_USER).isEqualTo(2L);
    }

    @Test
    void testUserStatusConstants() {
        // 测试用户状态常量
        assertThat(SystemConstants.USER_STATUS_NORMAL).isEqualTo(1);
        assertThat(SystemConstants.USER_STATUS_DISABLED).isEqualTo(0);
    }

    @Test
    void testCreditScoreConstant() {
        // 测试信用积分常量
        assertThat(SystemConstants.DEFAULT_CREDIT_SCORE).isEqualTo(100);
    }
}