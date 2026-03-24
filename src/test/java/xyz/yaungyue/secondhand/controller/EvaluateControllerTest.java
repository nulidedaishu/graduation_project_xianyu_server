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
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.request.EvaluateCreateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.EvaluateVO;
import xyz.yaungyue.secondhand.model.dto.response.PendingEvaluateOrderVO;
import xyz.yaungyue.secondhand.model.entity.Evaluate;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.EvaluateService;
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
 * 评价模块单元测试
 */
@ExtendWith(MockitoExtension.class)
class EvaluateControllerTest {

    @Mock
    private EvaluateService evaluateService;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EvaluateController evaluateController;

    private User testUser;
    private User testSeller;
    private Order testOrder;
    private Evaluate testEvaluate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("buyer");
        testUser.setNickname("买家");
        testUser.setAvatar("https://example.com/avatar.jpg");

        testSeller = new User();
        testSeller.setId(2L);
        testSeller.setUsername("seller");
        testSeller.setNickname("卖家");
        testSeller.setAvatar("https://example.com/seller.jpg");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderSn("ORDER20240320001");
        testOrder.setUserId(1L);
        testOrder.setStatus(3); // 待评价
        testOrder.setCreateTime(LocalDateTime.now());
        testOrder.setReceiveTime(LocalDateTime.now());

        testEvaluate = new Evaluate();
        testEvaluate.setId(1L);
        testEvaluate.setOrderId(1L);
        testEvaluate.setFromUserId(1L);
        testEvaluate.setToUserId(2L);
        testEvaluate.setScore(5);
        testEvaluate.setContent("商品很好，卖家服务态度不错！");
        testEvaluate.setType(1);
        testEvaluate.setCreateTime(LocalDateTime.now());
    }

    @Test
    void submitEvaluate_BuyerToSeller_Success() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(1L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setContent("好评！");
        request.setType(1);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.getById(1L)).thenReturn(testOrder);
            when(evaluateService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(evaluateService.save(any(Evaluate.class))).thenReturn(true);

            // When
            ApiResponse<EvaluateVO> response = evaluateController.submitEvaluate(request);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            verify(evaluateService).save(any(Evaluate.class));
        }
    }

    @Test
    void submitEvaluate_OrderNotFound() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(999L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setContent("好评！");
        request.setType(1);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.getById(999L)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> evaluateController.submitEvaluate(request));
            assertEquals(404, exception.getCode());
            assertEquals("订单不存在", exception.getMessage());
        }
    }

    @Test
    void submitEvaluate_NotBuyer() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(1L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setContent("好评！");
        request.setType(1);

        User otherUser = new User();
        otherUser.setId(3L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(otherUser);
            when(orderService.getById(1L)).thenReturn(testOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> evaluateController.submitEvaluate(request));
            assertEquals(403, exception.getCode());
            assertEquals("只有买家可以评价卖家", exception.getMessage());
        }
    }

    @Test
    void submitEvaluate_InvalidOrderStatus() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(1L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setContent("好评！");
        request.setType(1);

        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setUserId(1L);
        pendingOrder.setStatus(0); // 待付款状态，不能评价

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.getById(1L)).thenReturn(pendingOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> evaluateController.submitEvaluate(request));
            assertEquals(400, exception.getCode());
            assertEquals("当前订单状态不可评价", exception.getMessage());
        }
    }

    @Test
    void submitEvaluate_AlreadyEvaluated() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(1L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setContent("好评！");
        request.setType(1);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.getById(1L)).thenReturn(testOrder);
            when(evaluateService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> evaluateController.submitEvaluate(request));
            assertEquals(400, exception.getCode());
            assertEquals("您已经评价过了", exception.getMessage());
        }
    }

    @Test
    void submitEvaluate_SellerToBuyer_NotImplemented() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(1L);
        request.setToUserId(1L);
        request.setScore(5);
        request.setContent("买家很好！");
        request.setType(2); // 卖家评买家

        User seller = new User();
        seller.setId(2L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(seller);
            when(orderService.getById(1L)).thenReturn(testOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> evaluateController.submitEvaluate(request));
            assertEquals(403, exception.getCode());
            assertEquals("功能暂未开放", exception.getMessage());
        }
    }

    @Test
    void submitEvaluate_InvalidType() {
        // Given
        EvaluateCreateRequest request = new EvaluateCreateRequest();
        request.setOrderId(1L);
        request.setToUserId(2L);
        request.setScore(5);
        request.setContent("好评！");
        request.setType(3); // 无效类型

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.getById(1L)).thenReturn(testOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> evaluateController.submitEvaluate(request));
            assertEquals(400, exception.getCode());
            assertEquals("无效的评价类型", exception.getMessage());
        }
    }

    @Test
    void getUserEvaluates_Success() {
        // Given
        Long userId = 2L;
        Page<Evaluate> evaluatePage = new Page<>();
        evaluatePage.setCurrent(1);
        evaluatePage.setSize(10);
        evaluatePage.setTotal(1);
        evaluatePage.setRecords(Arrays.asList(testEvaluate));

        when(evaluateService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(evaluatePage);
        when(userService.getById(1L)).thenReturn(testUser);
        when(userService.getById(2L)).thenReturn(testSeller);

        // When
        ApiResponse<IPage<EvaluateVO>> response = evaluateController.getUserEvaluates(userId, 1, 10);

        // Then
        assertEquals(200, response.code());
        assertNotNull(response.data());
        assertEquals(1, response.data().getRecords().size());
    }

    @Test
    void getMyEvaluates_Success() {
        // Given
        Page<Evaluate> evaluatePage = new Page<>();
        evaluatePage.setCurrent(1);
        evaluatePage.setSize(10);
        evaluatePage.setTotal(1);
        evaluatePage.setRecords(Arrays.asList(testEvaluate));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(evaluateService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(evaluatePage);
            when(userService.getById(1L)).thenReturn(testUser);
            when(userService.getById(2L)).thenReturn(testSeller);

            // When
            ApiResponse<IPage<EvaluateVO>> response = evaluateController.getMyEvaluates(1, 10);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1, response.data().getRecords().size());
        }
    }

    @Test
    void getPendingOrders_Success() {
        // Given
        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderSn("ORDER001");
        order1.setStatus(3);
        order1.setCreateTime(LocalDateTime.now());
        order1.setReceiveTime(LocalDateTime.now());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setOrderSn("ORDER002");
        order2.setStatus(3);
        order2.setCreateTime(LocalDateTime.now());
        order2.setReceiveTime(LocalDateTime.now());

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(order1, order2));

            // When
            ApiResponse<java.util.List<PendingEvaluateOrderVO>> response = evaluateController.getPendingOrders();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(2, response.data().size());
            assertEquals("待评价", response.data().get(0).getStatusDesc());
        }
    }

    @Test
    void getPendingOrders_EmptyList() {
        // Given
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(orderService.list(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            ApiResponse<java.util.List<PendingEvaluateOrderVO>> response = evaluateController.getPendingOrders();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertTrue(response.data().isEmpty());
        }
    }
}
