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
import xyz.yaungyue.secondhand.model.dto.request.ChatMessageRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.ChatMessageVO;
import xyz.yaungyue.secondhand.model.dto.response.ChatSessionVO;
import xyz.yaungyue.secondhand.model.dto.response.ChatUnreadCountVO;
import xyz.yaungyue.secondhand.model.entity.ChatMessage;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.ChatMessageService;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 即时通讯模块单元测试
 */
@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private User testUser;
    private User otherUser;
    private Product testProduct;
    private ChatMessage testMessage;
    private ChatMessage replyMessage;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("user1");
        testUser.setNickname("用户1");
        testUser.setAvatar("https://example.com/avatar1.jpg");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("user2");
        otherUser.setNickname("用户2");
        otherUser.setAvatar("https://example.com/avatar2.jpg");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("iPhone 14 Pro");
        testProduct.setMainImage("https://example.com/iphone.jpg");

        testMessage = new ChatMessage();
        testMessage.setId(1L);
        testMessage.setSenderId(1L);
        testMessage.setReceiverId(2L);
        testMessage.setProductId(1L);
        testMessage.setContent("您好，这个商品还在吗？");
        testMessage.setMsgType(0);
        testMessage.setIsRead(0);
        testMessage.setSessionKey("1_2");
        testMessage.setCreateTime(LocalDateTime.now());

        replyMessage = new ChatMessage();
        replyMessage.setId(2L);
        replyMessage.setSenderId(2L);
        replyMessage.setReceiverId(1L);
        replyMessage.setProductId(1L);
        replyMessage.setContent("还在的，欢迎购买！");
        replyMessage.setMsgType(0);
        replyMessage.setIsRead(0);
        replyMessage.setSessionKey("1_2");
        replyMessage.setCreateTime(LocalDateTime.now());
    }

    @Test
    void sendMessage_Success() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setReceiverId(2L);
        request.setProductId(1L);
        request.setContent("您好，这个商品还在吗？");
        request.setMsgType(0);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(2L)).thenReturn(otherUser);
            when(chatMessageService.save(any(ChatMessage.class))).thenReturn(true);

            // When
            ApiResponse<ChatMessageVO> response = chatMessageController.sendMessage(request);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals("1_2", response.data().getSessionKey());
            verify(chatMessageService).save(any(ChatMessage.class));
        }
    }

    @Test
    void sendMessage_ToSelf() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setReceiverId(1L); // 发送给自己
        request.setContent("测试消息");

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatMessageController.sendMessage(request));
            assertEquals(400, exception.getCode());
            assertEquals("不能给自己发送消息", exception.getMessage());
        }
    }

    @Test
    void sendMessage_ReceiverNotFound() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setReceiverId(999L);
        request.setContent("测试消息");

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(999L)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatMessageController.sendMessage(request));
            assertEquals(404, exception.getCode());
            assertEquals("接收者不存在", exception.getMessage());
        }
    }

    @Test
    void sendMessage_SessionKeyGeneration() {
        // Given - 测试会话标识生成（小ID在前）
        ChatMessageRequest request = new ChatMessageRequest();
        request.setReceiverId(2L);
        request.setContent("测试消息");

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(userService.getById(2L)).thenReturn(otherUser);
            when(chatMessageService.save(any(ChatMessage.class))).thenReturn(true);

            // When
            ApiResponse<ChatMessageVO> response = chatMessageController.sendMessage(request);

            // Then - 会话标识应该是 "1_2"（小ID在前）
            assertEquals("1_2", response.data().getSessionKey());
        }
    }

    @Test
    void getChatMessages_Success() {
        // Given
        Long otherUserId = 2L;
        Page<ChatMessage> messagePage = new Page<>();
        messagePage.setCurrent(1);
        messagePage.setSize(20);
        messagePage.setTotal(2);
        messagePage.setRecords(Arrays.asList(testMessage, replyMessage));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(messagePage);
            when(userService.getById(1L)).thenReturn(testUser);
            when(userService.getById(2L)).thenReturn(otherUser);
            when(productService.getById(1L)).thenReturn(testProduct);

            // When
            ApiResponse<IPage<ChatMessageVO>> response = chatMessageController.getChatMessages(otherUserId, 1, 20);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(2, response.data().getRecords().size());
        }
    }

    @Test
    void getSessions_Success() {
        // Given
        List<ChatMessage> allMessages = Arrays.asList(
                replyMessage,  // 最新的消息
                testMessage
        );

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.list(any(LambdaQueryWrapper.class))).thenReturn(allMessages);
            when(userService.getById(2L)).thenReturn(otherUser);
            when(productService.getById(1L)).thenReturn(testProduct);
            when(chatMessageService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // When
            ApiResponse<List<ChatSessionVO>> response = chatMessageController.getSessions();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1, response.data().size());
            assertEquals("1_2", response.data().get(0).getSessionKey());
            assertEquals(2L, response.data().get(0).getOtherUserId());
            assertEquals("用户2", response.data().get(0).getOtherUserNickname());
        }
    }

    @Test
    void getSessions_Empty() {
        // Given
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            // When
            ApiResponse<List<ChatSessionVO>> response = chatMessageController.getSessions();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertTrue(response.data().isEmpty());
        }
    }

    @Test
    void markAsRead_Success() {
        // Given
        Long messageId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(otherUser); // 接收者标记已读
            when(chatMessageService.getById(messageId)).thenReturn(testMessage);
            when(chatMessageService.updateById(any(ChatMessage.class))).thenReturn(true);

            // When
            ApiResponse<Void> response = chatMessageController.markAsRead(messageId);

            // Then
            assertEquals(200, response.code());
            verify(chatMessageService).updateById(any(ChatMessage.class));
        }
    }

    @Test
    void markAsRead_MessageNotFound() {
        // Given
        Long messageId = 999L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.getById(messageId)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatMessageController.markAsRead(messageId));
            assertEquals(404, exception.getCode());
            assertEquals("消息不存在", exception.getMessage());
        }
    }

    @Test
    void markAsRead_NotReceiver() {
        // Given
        Long messageId = 1L;

        User thirdUser = new User();
        thirdUser.setId(3L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(thirdUser);
            when(chatMessageService.getById(messageId)).thenReturn(testMessage);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatMessageController.markAsRead(messageId));
            assertEquals(403, exception.getCode());
            assertEquals("无权操作该消息", exception.getMessage());
        }
    }

    @Test
    void markSessionAsRead_Success() {
        // Given
        Long otherUserId = 2L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.update(any(ChatMessage.class), any(LambdaQueryWrapper.class))).thenReturn(true);

            // When
            ApiResponse<Void> response = chatMessageController.markSessionAsRead(otherUserId);

            // Then
            assertEquals(200, response.code());
            verify(chatMessageService).update(any(ChatMessage.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void getUnreadCount_Success() {
        // Given
        List<ChatMessage> unreadMessages = Arrays.asList(
                testMessage,
                replyMessage,
                createMessage(3L, 3L, 1L, "msg3", "1_3")  // 不同会话
        );

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.count(any(LambdaQueryWrapper.class))).thenReturn(3L);
            when(chatMessageService.list(any(LambdaQueryWrapper.class))).thenReturn(unreadMessages);

            // When
            ApiResponse<ChatUnreadCountVO> response = chatMessageController.getUnreadCount();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(3, response.data().getTotalUnreadCount());
            assertEquals(2, response.data().getSessionCount()); // 2个不同会话
        }
    }

    @Test
    void getUnreadCount_NoUnread() {
        // Given
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(chatMessageService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(chatMessageService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            // When
            ApiResponse<ChatUnreadCountVO> response = chatMessageController.getUnreadCount();

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(0, response.data().getTotalUnreadCount());
            assertEquals(0, response.data().getSessionCount());
        }
    }

    private ChatMessage createMessage(Long id, Long senderId, Long receiverId, String content, String sessionKey) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setSessionKey(sessionKey);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}
