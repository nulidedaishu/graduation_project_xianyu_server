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
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.NoticeStatisticsVO;
import xyz.yaungyue.secondhand.model.dto.response.NoticeVO;
import xyz.yaungyue.secondhand.model.entity.Notice;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.NoticeService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通知公告模块单元测试
 */
@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {

    @Mock
    private NoticeService noticeService;

    @InjectMocks
    private NoticeController noticeController;

    private User testUser;
    private Notice testNotice;
    private Notice auditNotice;
    private Notice orderNotice;
    private Notice systemNotice;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        testNotice = new Notice();
        testNotice.setId(1L);
        testNotice.setUserId(1L);
        testNotice.setTitle("测试通知");
        testNotice.setContent("这是一条测试通知");
        testNotice.setType(3);
        testNotice.setIsRead(0);
        testNotice.setCreateTime(LocalDateTime.now());

        auditNotice = new Notice();
        auditNotice.setId(2L);
        auditNotice.setUserId(1L);
        auditNotice.setTitle("审核通知");
        auditNotice.setContent("您的商品已通过审核");
        auditNotice.setType(1);
        auditNotice.setIsRead(0);
        auditNotice.setCreateTime(LocalDateTime.now());

        orderNotice = new Notice();
        orderNotice.setId(3L);
        orderNotice.setUserId(1L);
        orderNotice.setTitle("订单通知");
        orderNotice.setContent("您有一个新订单");
        orderNotice.setType(2);
        orderNotice.setIsRead(1);
        orderNotice.setCreateTime(LocalDateTime.now());

        systemNotice = new Notice();
        systemNotice.setId(4L);
        systemNotice.setUserId(1L);
        systemNotice.setTitle("系统公告");
        systemNotice.setContent("系统维护通知");
        systemNotice.setType(3);
        systemNotice.setIsRead(0);
        systemNotice.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getNotices_Success() {
        // Given
        Page<Notice> noticePage = new Page<>();
        noticePage.setCurrent(1);
        noticePage.setSize(10);
        noticePage.setTotal(2);
        noticePage.setRecords(Arrays.asList(testNotice, systemNotice));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(noticePage);

            // When
            ApiResponse<IPage<NoticeVO>> response = noticeController.getNotices(1, 10, null);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(2, response.data().getRecords().size());
        }
    }

    @Test
    void getNotices_WithTypeFilter() {
        // Given
        Page<Notice> noticePage = new Page<>();
        noticePage.setCurrent(1);
        noticePage.setSize(10);
        noticePage.setTotal(1);
        noticePage.setRecords(Arrays.asList(auditNotice));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(noticePage);

            // When - 筛选审核通知
            ApiResponse<IPage<NoticeVO>> response = noticeController.getNotices(1, 10, 1);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1, response.data().getRecords().size());
            assertEquals("审核通知", response.data().getRecords().get(0).getTypeDesc());
        }
    }

    @Test
    void getNotices_EmptyList() {
        // Given
        Page<Notice> emptyPage = new Page<>();
        emptyPage.setCurrent(1);
        emptyPage.setSize(10);
        emptyPage.setTotal(0);
        emptyPage.setRecords(Collections.emptyList());

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(emptyPage);

            // When
            ApiResponse<IPage<NoticeVO>> response = noticeController.getNotices(1, 10, null);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertTrue(response.data().getRecords().isEmpty());
        }
    }

    @Test
    void getUnreadNotices_Success() {
        // Given
        Page<Notice> noticePage = new Page<>();
        noticePage.setCurrent(1);
        noticePage.setSize(10);
        noticePage.setTotal(2);
        noticePage.setRecords(Arrays.asList(testNotice, auditNotice));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(noticePage);

            // When
            ApiResponse<IPage<NoticeVO>> response = noticeController.getUnreadNotices(1, 10);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(2, response.data().getRecords().size());
        }
    }

    @Test
    void getStatistics_Success() {
        // Given
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.count(any(LambdaQueryWrapper.class)))
                    .thenReturn(4L)  // totalCount
                    .thenReturn(3L)  // unreadCount
                    .thenReturn(1L)  // auditUnreadCount
                    .thenReturn(0L)  // orderUnreadCount
                    .thenReturn(2L); // systemUnreadCount

            // When
            ApiResponse<NoticeStatisticsVO> response = noticeController.getStatistics();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(4, response.data().getTotalCount());
            assertEquals(3, response.data().getUnreadCount());
            assertEquals(1, response.data().getAuditUnreadCount());
            assertEquals(0, response.data().getOrderUnreadCount());
            assertEquals(2, response.data().getSystemUnreadCount());
        }
    }

    @Test
    void markAsRead_Success() {
        // Given
        Long noticeId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.getById(noticeId)).thenReturn(testNotice);
            when(noticeService.updateById(any(Notice.class))).thenReturn(true);

            // When
            ApiResponse<Void> response = noticeController.markAsRead(noticeId);

            // Then
            assertEquals(200, response.code());
            verify(noticeService).updateById(any(Notice.class));
        }
    }

    @Test
    void markAsRead_NoticeNotFound() {
        // Given
        Long noticeId = 999L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.getById(noticeId)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> noticeController.markAsRead(noticeId));
            assertEquals(404, exception.getCode());
            assertEquals("通知不存在", exception.getMessage());
        }
    }

    @Test
    void markAsRead_NotOwner() {
        // Given
        Long noticeId = 1L;

        Notice otherUserNotice = new Notice();
        otherUserNotice.setId(1L);
        otherUserNotice.setUserId(2L); // 其他用户的通知

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.getById(noticeId)).thenReturn(otherUserNotice);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> noticeController.markAsRead(noticeId));
            assertEquals(403, exception.getCode());
            assertEquals("无权操作该通知", exception.getMessage());
        }
    }

    @Test
    void markAllAsRead_Success() {
        // Given
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.update(any(Notice.class), any(LambdaQueryWrapper.class))).thenReturn(true);

            // When
            ApiResponse<Void> response = noticeController.markAllAsRead();

            // Then
            assertEquals(200, response.code());
            verify(noticeService).update(any(Notice.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void deleteNotice_Success() {
        // Given
        Long noticeId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.getById(noticeId)).thenReturn(testNotice);
            when(noticeService.removeById(noticeId)).thenReturn(true);

            // When
            ApiResponse<Void> response = noticeController.deleteNotice(noticeId);

            // Then
            assertEquals(200, response.code());
            verify(noticeService).removeById(noticeId);
        }
    }

    @Test
    void deleteNotice_NoticeNotFound() {
        // Given
        Long noticeId = 999L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.getById(noticeId)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> noticeController.deleteNotice(noticeId));
            assertEquals(404, exception.getCode());
            assertEquals("通知不存在", exception.getMessage());
        }
    }

    @Test
    void deleteNotice_NotOwner() {
        // Given
        Long noticeId = 1L;

        Notice otherUserNotice = new Notice();
        otherUserNotice.setId(1L);
        otherUserNotice.setUserId(2L); // 其他用户的通知

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(noticeService.getById(noticeId)).thenReturn(otherUserNotice);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> noticeController.deleteNotice(noticeId));
            assertEquals(403, exception.getCode());
            assertEquals("无权删除该通知", exception.getMessage());
        }
    }

    @Test
    void sendSystemNotice_Success() {
        // Given
        Notice newNotice = new Notice();
        newNotice.setUserId(2L);
        newNotice.setTitle("系统通知");
        newNotice.setContent("系统维护公告");
        newNotice.setType(3);

        when(noticeService.save(any(Notice.class))).thenReturn(true);

        // When
        ApiResponse<NoticeVO> response = noticeController.sendSystemNotice(newNotice);

        // Then
        assertEquals(200, response.code());
        assertNotNull(response.data());
        assertEquals("系统公告", response.data().getTypeDesc());
        verify(noticeService).save(any(Notice.class));
    }

    @Test
    void sendSystemNotice_MissingUserId() {
        // Given
        Notice newNotice = new Notice();
        newNotice.setTitle("系统通知");
        newNotice.setContent("系统维护公告");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> noticeController.sendSystemNotice(newNotice));
        assertEquals(400, exception.getCode());
        assertEquals("接收用户ID不能为空", exception.getMessage());
    }

    @Test
    void sendSystemNotice_MissingTitle() {
        // Given
        Notice newNotice = new Notice();
        newNotice.setUserId(2L);
        newNotice.setContent("系统维护公告");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> noticeController.sendSystemNotice(newNotice));
        assertEquals(400, exception.getCode());
        assertEquals("通知标题不能为空", exception.getMessage());
    }

    @Test
    void sendSystemNotice_MissingContent() {
        // Given
        Notice newNotice = new Notice();
        newNotice.setUserId(2L);
        newNotice.setTitle("系统通知");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> noticeController.sendSystemNotice(newNotice));
        assertEquals(400, exception.getCode());
        assertEquals("通知内容不能为空", exception.getMessage());
    }

    @Test
    void sendSystemNotice_DefaultType() {
        // Given
        Notice newNotice = new Notice();
        newNotice.setUserId(2L);
        newNotice.setTitle("系统通知");
        newNotice.setContent("系统维护公告");
        // type为null，应该默认为3

        when(noticeService.save(any(Notice.class))).thenReturn(true);

        // When
        ApiResponse<NoticeVO> response = noticeController.sendSystemNotice(newNotice);

        // Then
        assertEquals(200, response.code());
        verify(noticeService).save(argThat(notice -> notice.getType() == 3));
    }
}
