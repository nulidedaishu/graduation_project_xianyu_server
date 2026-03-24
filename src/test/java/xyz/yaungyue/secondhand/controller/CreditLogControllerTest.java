package xyz.yaungyue.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.CreditLogVO;
import xyz.yaungyue.secondhand.model.dto.response.CreditStatisticsVO;
import xyz.yaungyue.secondhand.model.entity.CreditLog;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.CreditLogService;
import xyz.yaungyue.secondhand.service.OrderService;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 信用积分模块单元测试
 */
@ExtendWith(MockitoExtension.class)
class CreditLogControllerTest {

    @Mock
    private CreditLogService creditLogService;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CreditLogController creditLogController;

    private User testUser;
    private CreditLog incomeLog1;
    private CreditLog incomeLog2;
    private CreditLog expenseLog;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setCreditScore(150);

        incomeLog1 = new CreditLog();
        incomeLog1.setId(1L);
        incomeLog1.setUserId(1L);
        incomeLog1.setOrderId(1L);
        incomeLog1.setChangeValue(10);
        incomeLog1.setReason("交易完成奖励");
        incomeLog1.setCreateTime(LocalDateTime.now());

        incomeLog2 = new CreditLog();
        incomeLog2.setId(2L);
        incomeLog2.setUserId(1L);
        incomeLog2.setOrderId(2L);
        incomeLog2.setChangeValue(5);
        incomeLog2.setReason("获得好评奖励");
        incomeLog2.setCreateTime(LocalDateTime.now());

        expenseLog = new CreditLog();
        expenseLog.setId(3L);
        expenseLog.setUserId(1L);
        expenseLog.setOrderId(null);
        expenseLog.setChangeValue(-5);
        expenseLog.setReason("违规扣分");
        expenseLog.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getCreditLogs_Success() {
        // Given
        Page<CreditLog> creditLogPage = new Page<>();
        creditLogPage.setCurrent(1);
        creditLogPage.setSize(10);
        creditLogPage.setTotal(3);
        creditLogPage.setRecords(Arrays.asList(incomeLog1, incomeLog2, expenseLog));

        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderSn("ORDER001");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setOrderSn("ORDER002");

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(creditLogService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(creditLogPage);
            when(orderService.getById(1L)).thenReturn(order1);
            when(orderService.getById(2L)).thenReturn(order2);

            // When
            ApiResponse<IPage<CreditLogVO>> response = creditLogController.getCreditLogs(1, 10, null);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(3, response.data().getRecords().size());
            assertEquals("+10", response.data().getRecords().get(0).getChangeValueDisplay());
            assertEquals("-5", response.data().getRecords().get(2).getChangeValueDisplay());
        }
    }

    @Test
    void getCreditLogs_IncomeFilter() {
        // Given
        Page<CreditLog> creditLogPage = new Page<>();
        creditLogPage.setCurrent(1);
        creditLogPage.setSize(10);
        creditLogPage.setTotal(2);
        creditLogPage.setRecords(Arrays.asList(incomeLog1, incomeLog2));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(creditLogService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(creditLogPage);

            // When - 只查询收入
            ApiResponse<IPage<CreditLogVO>> response = creditLogController.getCreditLogs(1, 10, "income");

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(2, response.data().getRecords().size());
            assertTrue(response.data().getRecords().stream()
                    .allMatch(log -> log.getChangeValue() > 0));
        }
    }

    @Test
    void getCreditLogs_ExpenseFilter() {
        // Given
        Page<CreditLog> creditLogPage = new Page<>();
        creditLogPage.setCurrent(1);
        creditLogPage.setSize(10);
        creditLogPage.setTotal(1);
        creditLogPage.setRecords(Collections.singletonList(expenseLog));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(creditLogService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(creditLogPage);

            // When - 只查询支出
            ApiResponse<IPage<CreditLogVO>> response = creditLogController.getCreditLogs(1, 10, "expense");

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1, response.data().getRecords().size());
            assertTrue(response.data().getRecords().get(0).getChangeValue() < 0);
        }
    }

    @Test
    void getCreditLogs_EmptyList() {
        // Given
        Page<CreditLog> emptyPage = new Page<>();
        emptyPage.setCurrent(1);
        emptyPage.setSize(10);
        emptyPage.setTotal(0);
        emptyPage.setRecords(Collections.emptyList());

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(creditLogService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(emptyPage);

            // When
            ApiResponse<IPage<CreditLogVO>> response = creditLogController.getCreditLogs(1, 10, null);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertTrue(response.data().getRecords().isEmpty());
        }
    }

    @Test
    void getStatistics_ExcellentLevel() {
        // Given - 信用分150，属于"优秀"
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setCreditScore(150);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(1L)).thenReturn(updatedUser);
            when(creditLogService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(incomeLog1, incomeLog2))  // 收入列表
                    .thenReturn(Collections.singletonList(expenseLog)); // 支出列表

            // When
            ApiResponse<CreditStatisticsVO> response = creditLogController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(150, response.data().getCurrentCredit());
            assertEquals(15, response.data().getTotalIncome());  // 10 + 5
            assertEquals(5, response.data().getTotalExpense());  // |-5|
            assertEquals("优秀", response.data().getCreditLevel());
            assertEquals(1, response.data().getTransactionRewardCount()); // "交易完成"
            assertEquals(1, response.data().getGoodEvaluateCount());      // "好评"
        }
    }

    @Test
    void getStatistics_ExcellentLevel_200Score() {
        // Given - 信用分200，属于"极好"
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setCreditScore(200);

        CreditLog log1 = new CreditLog();
        log1.setChangeValue(50);
        log1.setReason("交易完成奖励");

        CreditLog log2 = new CreditLog();
        log2.setChangeValue(50);
        log2.setReason("交易完成奖励");

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(1L)).thenReturn(updatedUser);
            when(creditLogService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(log1, log2))
                    .thenReturn(Collections.emptyList());

            // When
            ApiResponse<CreditStatisticsVO> response = creditLogController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(200, response.data().getCurrentCredit());
            assertEquals("极好", response.data().getCreditLevel());
        }
    }

    @Test
    void getStatistics_GoodLevel() {
        // Given - 信用分100，属于"良好"
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setCreditScore(100);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(1L)).thenReturn(updatedUser);
            when(creditLogService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Collections.emptyList());

            // When
            ApiResponse<CreditStatisticsVO> response = creditLogController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(100, response.data().getCurrentCredit());
            assertEquals("良好", response.data().getCreditLevel());
        }
    }

    @Test
    void getStatistics_GeneralLevel() {
        // Given - 信用分50，属于"一般"
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setCreditScore(50);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(1L)).thenReturn(updatedUser);
            when(creditLogService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Collections.emptyList());

            // When
            ApiResponse<CreditStatisticsVO> response = creditLogController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(50, response.data().getCurrentCredit());
            assertEquals("一般", response.data().getCreditLevel());
        }
    }

    @Test
    void getStatistics_LowLevel() {
        // Given - 信用分30，属于"较低"
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setCreditScore(30);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(1L)).thenReturn(updatedUser);
            when(creditLogService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Collections.emptyList());

            // When
            ApiResponse<CreditStatisticsVO> response = creditLogController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(30, response.data().getCurrentCredit());
            assertEquals("较低", response.data().getCreditLevel());
        }
    }

    @Test
    void getStatistics_NullCreditScore() {
        // Given - 信用分为null，应该默认为0
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setCreditScore(null);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(1L)).thenReturn(updatedUser);
            when(creditLogService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Collections.emptyList());

            // When
            ApiResponse<CreditStatisticsVO> response = creditLogController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(0, response.data().getCurrentCredit());
            assertEquals("较低", response.data().getCreditLevel());
        }
    }
}
