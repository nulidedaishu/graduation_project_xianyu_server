package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.request.ChatMessageRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.ChatMessageVO;
import xyz.yaungyue.secondhand.model.dto.response.ChatSessionVO;
import xyz.yaungyue.secondhand.model.dto.response.ChatUnreadCountVO;
import xyz.yaungyue.secondhand.model.dto.response.MessagePushEvent;
import xyz.yaungyue.secondhand.model.dto.response.NoticeVO;
import xyz.yaungyue.secondhand.model.dto.response.TotalUnreadCountVO;
import xyz.yaungyue.secondhand.model.dto.response.UnifiedSessionVO;
import xyz.yaungyue.secondhand.service.MessagePushService;
import xyz.yaungyue.secondhand.service.NoticeService;
import xyz.yaungyue.secondhand.model.entity.ChatMessage;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.ChatMessageService;
import xyz.yaungyue.secondhand.service.MessagePushService;
import xyz.yaungyue.secondhand.service.NoticeService;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.UserService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 即时通讯Controller
 *
 * @author yaung
 * @date 2026-03-20
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "即时通讯", description = "聊天消息相关接口")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final ProductService productService;
    private final NoticeService noticeService;
    private final MessagePushService messagePushService;

    /**
     * 发送消息
     * @param request 消息请求
     * @return 消息信息
     */
    @PostMapping("/messages")
    @SaCheckLogin(type = "user")
    @Operation(summary = "发送消息", description = "发送聊天消息给指定用户")
    public ApiResponse<ChatMessageVO> sendMessage(@RequestBody @Valid ChatMessageRequest request) {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 不能给自己发送消息
        if (currentUser.getId().equals(request.getReceiverId())) {
            throw new BusinessException(400, "不能给自己发送消息");
        }

        // 检查接收者是否存在
        User receiver = userService.getById(request.getReceiverId());
        if (receiver == null) {
            throw new BusinessException(404, "接收者不存在");
        }

        // 生成会话标识 (较小的ID在前)
        String sessionKey = generateSessionKey(currentUser.getId(), request.getReceiverId());

        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setSenderId(currentUser.getId());
        message.setReceiverId(request.getReceiverId());
        message.setProductId(request.getProductId());
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType() != null ? request.getMsgType() : 0);
        message.setIsRead(0);
        message.setSessionKey(sessionKey);

        boolean success = chatMessageService.save(message);
        if (success) {
            ChatMessageVO vo = convertToVO(message);

            // SSE推送消息给接收者
            MessagePushEvent event = MessagePushEvent.chatEvent(
                    vo,
                    messagePushService.nextSequence(),
                    request.getReceiverId()
            );
            messagePushService.pushToUser(request.getReceiverId(), event);

            return ApiResponse.success(vo);
        }
        return ApiResponse.error(500, "发送消息失败");
    }

    /**
     * 获取聊天记录
     * @param userId 对方用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 聊天记录
     */
    @GetMapping("/messages/{userId}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取聊天记录", description = "获取与指定用户的聊天记录")
    public ApiResponse<IPage<ChatMessageVO>> getChatMessages(
            @Parameter(description = "对方用户ID", example = "2") @PathVariable Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {

        User currentUser = SaTokenUtil.getCurrentUser();

        // 生成会话标识
        String sessionKey = generateSessionKey(currentUser.getId(), userId);

        IPage<ChatMessage> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionKey, sessionKey)
                .orderByDesc(ChatMessage::getCreateTime);

        IPage<ChatMessage> messagePage = chatMessageService.page(pageParam, queryWrapper);

        List<ChatMessageVO> voList = messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<ChatMessageVO> voPage = new Page<>();
        voPage.setCurrent(messagePage.getCurrent());
        voPage.setSize(messagePage.getSize());
        voPage.setTotal(messagePage.getTotal());
        voPage.setPages(messagePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取会话列表
     * @return 会话列表
     */
    @GetMapping("/sessions")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取会话列表", description = "获取当前用户的会话列表")
    public ApiResponse<List<ChatSessionVO>> getSessions() {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 查询当前用户参与的所有会话
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(qw -> qw.eq(ChatMessage::getSenderId, currentUser.getId())
                        .or()
                        .eq(ChatMessage::getReceiverId, currentUser.getId()))
                .orderByDesc(ChatMessage::getCreateTime);

        List<ChatMessage> allMessages = chatMessageService.list(queryWrapper);

        // 按会话分组，获取每个会话的最新消息
        Map<String, ChatMessage> sessionLastMessageMap = new LinkedHashMap<>();
        for (ChatMessage message : allMessages) {
            if (!sessionLastMessageMap.containsKey(message.getSessionKey())) {
                sessionLastMessageMap.put(message.getSessionKey(), message);
            }
        }

        // 构建会话列表
        List<ChatSessionVO> sessionList = new ArrayList<>();
        for (Map.Entry<String, ChatMessage> entry : sessionLastMessageMap.entrySet()) {
            String sessionKey = entry.getKey();
            ChatMessage lastMessage = entry.getValue();

            // 确定对方用户ID
            Long otherUserId = lastMessage.getSenderId().equals(currentUser.getId())
                    ? lastMessage.getReceiverId()
                    : lastMessage.getSenderId();

            ChatSessionVO sessionVO = buildSessionVO(sessionKey, otherUserId, lastMessage, currentUser.getId());
            sessionList.add(sessionVO);
        }

        return ApiResponse.success(sessionList);
    }

    /**
     * 标记消息已读
     * @param id 消息ID
     * @return 操作结果
     */
    @PutMapping("/messages/{id}/read")
    @SaCheckLogin(type = "user")
    @Operation(summary = "标记已读", description = "标记单条消息为已读状态")
    public ApiResponse<Void> markAsRead(
            @Parameter(description = "消息ID", example = "1") @PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();

        ChatMessage message = chatMessageService.getById(id);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 验证是否是接收者
        if (!message.getReceiverId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权操作该消息");
        }

        message.setIsRead(1);
        boolean success = chatMessageService.updateById(message);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "操作失败");
    }

    /**
     * 标记会话所有消息已读
     * @param userId 对方用户ID
     * @return 操作结果
     */
    @PutMapping("/sessions/{userId}/read")
    @SaCheckLogin(type = "user")
    @Operation(summary = "标记会话已读", description = "标记与指定用户的会话所有消息为已读")
    public ApiResponse<Void> markSessionAsRead(
            @Parameter(description = "对方用户ID", example = "2") @PathVariable Long userId) {
        User currentUser = SaTokenUtil.getCurrentUser();

        String sessionKey = generateSessionKey(currentUser.getId(), userId);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionKey, sessionKey)
                .eq(ChatMessage::getReceiverId, currentUser.getId())
                .eq(ChatMessage::getIsRead, 0);

        ChatMessage updateMessage = new ChatMessage();
        updateMessage.setIsRead(1);

        chatMessageService.update(updateMessage, queryWrapper);
        return ApiResponse.success();
    }

    /**
     * 获取未读消息数
     * @return 未读消息统计
     */
    @GetMapping("/unread-count")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息总数")
    public ApiResponse<ChatUnreadCountVO> getUnreadCount() {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getReceiverId, currentUser.getId())
                .eq(ChatMessage::getIsRead, 0);

        long totalUnread = chatMessageService.count(queryWrapper);

        // 统计有多少个会话有未读消息
        List<ChatMessage> unreadMessages = chatMessageService.list(queryWrapper);
        long sessionCount = unreadMessages.stream()
                .map(ChatMessage::getSessionKey)
                .distinct()
                .count();

        ChatUnreadCountVO vo = new ChatUnreadCountVO();
        vo.setTotalUnreadCount((int) totalUnread);
        vo.setSessionCount((int) sessionCount);

        return ApiResponse.success(vo);
    }

    /**
     * 获取总未读消息数（聊天+通知）
     * @return 总未读消息统计
     */
    @GetMapping("/messages/unread-count")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取总未读消息数", description = "获取当前用户的聊天和通知总未读数")
    public ApiResponse<TotalUnreadCountVO> getTotalUnreadCount() {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 查询聊天未读数
        LambdaQueryWrapper<ChatMessage> chatWrapper = new LambdaQueryWrapper<>();
        chatWrapper.eq(ChatMessage::getReceiverId, currentUser.getId())
                .eq(ChatMessage::getIsRead, 0);
        long chatUnread = chatMessageService.count(chatWrapper);

        // 查询通知未读数
        LambdaQueryWrapper<xyz.yaungyue.secondhand.model.entity.Notice> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(xyz.yaungyue.secondhand.model.entity.Notice::getUserId, currentUser.getId())
                .eq(xyz.yaungyue.secondhand.model.entity.Notice::getIsRead, 0);
        long noticeUnread = noticeService.count(noticeWrapper);

        TotalUnreadCountVO vo = TotalUnreadCountVO.builder()
                .total((int) (chatUnread + noticeUnread))
                .chat((int) chatUnread)
                .notice((int) noticeUnread)
                .build();

        return ApiResponse.success(vo);
    }

    /**
     * 获取统一会话列表（包含用户会话和系统通知会话）
     * @param page 页码
     * @param size 每页数量
     * @return 统一会话列表
     */
    @GetMapping("/sessions/unified")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取统一会话列表", description = "获取包含用户聊天和系统通知的统一会话列表，系统通知置顶")
    public ApiResponse<IPage<UnifiedSessionVO>> getUnifiedSessions(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {

        User currentUser = SaTokenUtil.getCurrentUser();
        List<UnifiedSessionVO> allSessions = new ArrayList<>();

        // 1. 构建系统通知会话（始终置顶）
        LambdaQueryWrapper<xyz.yaungyue.secondhand.model.entity.Notice> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(xyz.yaungyue.secondhand.model.entity.Notice::getUserId, currentUser.getId())
                .orderByDesc(xyz.yaungyue.secondhand.model.entity.Notice::getCreateTime);
        List<xyz.yaungyue.secondhand.model.entity.Notice> notices = noticeService.list(noticeWrapper);

        if (!notices.isEmpty()) {
            xyz.yaungyue.secondhand.model.entity.Notice lastNotice = notices.get(0);
            long noticeUnreadCount = notices.stream()
                    .filter(n -> n.getIsRead() != null && n.getIsRead() == 0)
                    .count();

            UnifiedSessionVO systemSession = UnifiedSessionVO.builder()
                    .sessionType("system")
                    .sessionId("system")
                    .systemTitle("系统通知")
                    .systemIcon("/icons/system-notification.png")
                    .lastMessage(lastNotice.getTitle())
                    .lastMsgType(2) // 通知类型
                    .lastMessageTime(lastNotice.getCreateTime())
                    .unreadCount((int) noticeUnreadCount)
                    .isPinned(true)
                    .build();
            allSessions.add(systemSession);
        } else {
            // 即使没有通知也显示系统通知会话
            UnifiedSessionVO systemSession = UnifiedSessionVO.builder()
                    .sessionType("system")
                    .sessionId("system")
                    .systemTitle("系统通知")
                    .systemIcon("/icons/system-notification.png")
                    .lastMessage("暂无新通知")
                    .lastMsgType(2)
                    .lastMessageTime(LocalDateTime.now())
                    .unreadCount(0)
                    .isPinned(true)
                    .build();
            allSessions.add(systemSession);
        }

        // 2. 构建用户聊天会话列表
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(qw -> qw.eq(ChatMessage::getSenderId, currentUser.getId())
                        .or()
                        .eq(ChatMessage::getReceiverId, currentUser.getId()))
                .orderByDesc(ChatMessage::getCreateTime);

        List<ChatMessage> allMessages = chatMessageService.list(queryWrapper);

        // 按会话分组，获取每个会话的最新消息
        Map<String, ChatMessage> sessionLastMessageMap = new LinkedHashMap<>();
        for (ChatMessage message : allMessages) {
            if (!sessionLastMessageMap.containsKey(message.getSessionKey())) {
                sessionLastMessageMap.put(message.getSessionKey(), message);
            }
        }

        // 构建用户会话列表
        for (Map.Entry<String, ChatMessage> entry : sessionLastMessageMap.entrySet()) {
            String sessionKey = entry.getKey();
            ChatMessage lastMessage = entry.getValue();

            // 确定对方用户ID
            Long otherUserId = lastMessage.getSenderId().equals(currentUser.getId())
                    ? lastMessage.getReceiverId()
                    : lastMessage.getSenderId();

            UnifiedSessionVO sessionVO = buildUnifiedSessionVO(sessionKey, otherUserId, lastMessage, currentUser.getId());
            allSessions.add(sessionVO);
        }

        // 分页处理
        int total = allSessions.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        List<UnifiedSessionVO> pageRecords = new ArrayList<>();
        if (start < total) {
            pageRecords = allSessions.subList(start, end);
        }

        IPage<UnifiedSessionVO> resultPage = new Page<>();
        resultPage.setCurrent(page);
        resultPage.setSize(size);
        resultPage.setTotal(total);
        resultPage.setPages((total + size - 1) / size);
        resultPage.setRecords(pageRecords);

        return ApiResponse.success(resultPage);
    }

    /**
     * 游标分页获取聊天记录
     * @param userId 对方用户ID
     * @param lastId 上一页最后一条消息ID（游标）
     * @param size 每页数量
     * @return 聊天记录
     */
    @GetMapping("/messages/{userId}/cursor")
    @SaCheckLogin(type = "user")
    @Operation(summary = "游标分页获取聊天记录", description = "使用游标分页获取聊天记录，避免深度分页性能问题")
    public ApiResponse<List<ChatMessageVO>> getChatMessagesByCursor(
            @Parameter(description = "对方用户ID", example = "2") @PathVariable Long userId,
            @Parameter(description = "上一页最后一条消息ID（首次不传）", example = "100") @RequestParam(name = "lastId", required = false) Long lastId,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {

        User currentUser = SaTokenUtil.getCurrentUser();
        String sessionKey = generateSessionKey(currentUser.getId(), userId);

        // 限制每页数量范围，防止非法参数
        size = (size == null || size < 1) ? 20 : Math.min(size, 50);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionKey, sessionKey);

        // 使用游标分页
        if (lastId != null && lastId > 0) {
            queryWrapper.lt(ChatMessage::getId, lastId);
        }

        // 按ID升序排列（老消息在前，新消息在后），这样前端可以直接显示
        queryWrapper.orderByAsc(ChatMessage::getId)
                .last("LIMIT " + size);

        List<ChatMessage> messages = chatMessageService.list(queryWrapper);

        List<ChatMessageVO> voList = messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ApiResponse.success(voList);
    }

    /**
     * 生成会话标识
     */
    private String generateSessionKey(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    /**
     * 构建会话VO
     */
    private ChatSessionVO buildSessionVO(String sessionKey, Long otherUserId, ChatMessage lastMessage, Long currentUserId) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionKey(sessionKey);
        vo.setOtherUserId(otherUserId);

        // 查询对方用户信息
        User otherUser = userService.getById(otherUserId);
        if (otherUser != null) {
            vo.setOtherUserNickname(otherUser.getNickname());
            vo.setOtherUserAvatar(otherUser.getAvatar());
        }

        // 查询商品信息
        if (lastMessage.getProductId() != null) {
            Product product = productService.getById(lastMessage.getProductId());
            if (product != null) {
                vo.setProductId(product.getId());
                vo.setProductTitle(product.getTitle());
                vo.setProductImage(product.getMainImage());
            }
        }

        // 最后一条消息
        vo.setLastMessage(lastMessage.getContent());
        vo.setLastMsgType(lastMessage.getMsgType());
        vo.setLastMessageTime(lastMessage.getCreateTime());

        // 统计未读消息数
        LambdaQueryWrapper<ChatMessage> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(ChatMessage::getSessionKey, sessionKey)
                .eq(ChatMessage::getReceiverId, currentUserId)
                .eq(ChatMessage::getIsRead, 0);
        int unreadCount = (int) chatMessageService.count(unreadWrapper);
        vo.setUnreadCount(unreadCount);

        return vo;
    }

    /**
     * 转换为VO
     */
    private ChatMessageVO convertToVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setProductId(message.getProductId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setIsRead(message.getIsRead());
        vo.setSessionKey(message.getSessionKey());
        vo.setCreateTime(message.getCreateTime());

        // 消息类型描述
        String msgTypeDesc = message.getMsgType() != null && message.getMsgType() == 1 ? "图片" : "文字";
        vo.setMsgTypeDesc(msgTypeDesc);

        // 查询发送者信息
        User sender = userService.getById(message.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        // 查询接收者信息
        User receiver = userService.getById(message.getReceiverId());
        if (receiver != null) {
            vo.setReceiverNickname(receiver.getNickname());
            vo.setReceiverAvatar(receiver.getAvatar());
        }

        // 查询商品信息
        if (message.getProductId() != null) {
            Product product = productService.getById(message.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductImage(product.getMainImage());
            }
        }

        return vo;
    }

    /**
     * 删除会话
     * @param sessionKey 会话标识
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{sessionKey}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "删除会话", description = "删除当前用户参与的指定会话的所有聊天消息")
    public ApiResponse<Void> deleteSession(
            @Parameter(description = "会话标识", example = "1_2") @PathVariable String sessionKey) {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 验证sessionKey格式并检查当前用户是否有权限
        String[] userIds = sessionKey.split("_");
        if (userIds.length != 2) {
            throw new BusinessException(400, "无效的会话标识");
        }

        try {
            Long userId1 = Long.valueOf(userIds[0]);
            Long userId2 = Long.valueOf(userIds[1]);

            // 验证当前用户是会话的参与者
            if (!currentUser.getId().equals(userId1) && !currentUser.getId().equals(userId2)) {
                throw new BusinessException(403, "无权删除该会话");
            }

            // 删除该会话的所有消息
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getSessionKey, sessionKey);
            boolean success = chatMessageService.remove(queryWrapper);

            if (success) {
                return ApiResponse.success();
            }
            return ApiResponse.error(500, "删除会话失败");
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "无效的会话标识格式");
        }
    }

    /**
     * 构建统一会话VO
     */
    private UnifiedSessionVO buildUnifiedSessionVO(String sessionKey, Long otherUserId, ChatMessage lastMessage, Long currentUserId) {
        User otherUser = userService.getById(otherUserId);

        UnifiedSessionVO.UnifiedSessionVOBuilder builder = UnifiedSessionVO.builder()
                .sessionType("user")
                .sessionId(sessionKey)
                .otherUserId(otherUserId)
                .lastMessage(lastMessage.getContent())
                .lastMsgType(lastMessage.getMsgType())
                .lastMessageTime(lastMessage.getCreateTime())
                .isPinned(false);

        if (otherUser != null) {
            builder.otherUserName(otherUser.getNickname())
                    .otherUserAvatar(otherUser.getAvatar());
        }

        // 查询商品信息
        if (lastMessage.getProductId() != null) {
            Product product = productService.getById(lastMessage.getProductId());
            if (product != null) {
                builder.productId(product.getId())
                        .productTitle(product.getTitle())
                        .productImage(product.getMainImage());
            }
        }

        // 统计未读消息数
        LambdaQueryWrapper<ChatMessage> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(ChatMessage::getSessionKey, sessionKey)
                .eq(ChatMessage::getReceiverId, currentUserId)
                .eq(ChatMessage::getIsRead, 0);
        int unreadCount = (int) chatMessageService.count(unreadWrapper);
        builder.unreadCount(unreadCount);

        return builder.build();
    }
}
