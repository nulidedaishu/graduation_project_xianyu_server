package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.MessagePushEvent;
import xyz.yaungyue.secondhand.model.dto.response.NoticeSessionVO;
import xyz.yaungyue.secondhand.model.dto.response.NoticeStatisticsVO;
import xyz.yaungyue.secondhand.model.dto.response.NoticeVO;
import xyz.yaungyue.secondhand.model.entity.Notice;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.MessagePushService;
import xyz.yaungyue.secondhand.service.NoticeService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告Controller
 *
 * @author yaung
 * @date 2026-03-20
 */
@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "通知公告管理", description = "通知公告相关接口")
public class NoticeController {

    private final NoticeService noticeService;
    private final MessagePushService messagePushService;

    /**
     * 获取通知列表
     * @param page 页码
     * @param size 每页数量
     * @param type 类型筛选
     * @return 通知列表
     */
    @GetMapping
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
    public ApiResponse<IPage<NoticeVO>> getNotices(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Parameter(description = "类型(1-审核通知, 2-订单通知, 3-系统公告)", example = "1") @RequestParam(name = "type", required = false) Integer type) {

        User currentUser = SaTokenUtil.getCurrentUser();

        IPage<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(type != null, Notice::getType, type)
                .orderByDesc(Notice::getCreateTime);

        IPage<Notice> noticePage = noticeService.page(pageParam, queryWrapper);

        List<NoticeVO> voList = noticePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<NoticeVO> voPage = new Page<>();
        voPage.setCurrent(noticePage.getCurrent());
        voPage.setSize(noticePage.getSize());
        voPage.setTotal(noticePage.getTotal());
        voPage.setPages(noticePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取未读通知
     * @param page 页码
     * @param size 每页数量
     * @return 未读通知列表
     */
    @GetMapping("/unread")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取未读通知", description = "获取当前用户的未读通知列表")
    public ApiResponse<IPage<NoticeVO>> getUnreadNotices(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size) {

        User currentUser = SaTokenUtil.getCurrentUser();

        IPage<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0)
                .orderByDesc(Notice::getCreateTime);

        IPage<Notice> noticePage = noticeService.page(pageParam, queryWrapper);

        List<NoticeVO> voList = noticePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<NoticeVO> voPage = new Page<>();
        voPage.setCurrent(noticePage.getCurrent());
        voPage.setSize(noticePage.getSize());
        voPage.setTotal(noticePage.getTotal());
        voPage.setPages(noticePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取通知统计
     * @return 通知统计信息
     */
    @GetMapping("/statistics")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取通知统计", description = "获取当前用户的通知统计信息")
    public ApiResponse<NoticeStatisticsVO> getStatistics() {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<Notice> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Notice::getUserId, currentUser.getId());
        int totalCount = (int) noticeService.count(totalWrapper);

        LambdaQueryWrapper<Notice> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0);
        int unreadCount = (int) noticeService.count(unreadWrapper);

        // 按类型统计未读数量
        LambdaQueryWrapper<Notice> auditWrapper = new LambdaQueryWrapper<>();
        auditWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0)
                .eq(Notice::getType, 1);
        int auditUnreadCount = (int) noticeService.count(auditWrapper);

        LambdaQueryWrapper<Notice> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0)
                .eq(Notice::getType, 2);
        int orderUnreadCount = (int) noticeService.count(orderWrapper);

        LambdaQueryWrapper<Notice> systemWrapper = new LambdaQueryWrapper<>();
        systemWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0)
                .eq(Notice::getType, 3);
        int systemUnreadCount = (int) noticeService.count(systemWrapper);

        NoticeStatisticsVO vo = new NoticeStatisticsVO();
        vo.setTotalCount(totalCount);
        vo.setUnreadCount(unreadCount);
        vo.setAuditUnreadCount(auditUnreadCount);
        vo.setOrderUnreadCount(orderUnreadCount);
        vo.setSystemUnreadCount(systemUnreadCount);

        return ApiResponse.success(vo);
    }

    /**
     * 标记通知已读
     * @param id 通知ID
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    @SaCheckLogin(type = "user")
    @Operation(summary = "标记已读", description = "标记单条通知为已读状态")
    public ApiResponse<Void> markAsRead(
            @Parameter(description = "通知ID", example = "1") @PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();

        Notice notice = noticeService.getById(id);
        if (notice == null) {
            throw new BusinessException(404, "通知不存在");
        }

        // 验证是否是自己的通知
        if (!notice.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权操作该通知");
        }

        notice.setIsRead(1);
        boolean success = noticeService.updateById(notice);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "操作失败");
    }

    /**
     * 标记所有通知已读
     * @return 操作结果
     */
    @PutMapping("/read-all")
    @SaCheckLogin(type = "user")
    @Operation(summary = "全部已读", description = "标记当前用户的所有通知为已读状态")
    public ApiResponse<Void> markAllAsRead() {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0);

        Notice updateNotice = new Notice();
        updateNotice.setIsRead(1);

        boolean success = noticeService.update(updateNotice, queryWrapper);
        return ApiResponse.success();
    }

    /**
     * 删除通知
     * @param id 通知ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "删除通知", description = "删除单条通知")
    public ApiResponse<Void> deleteNotice(
            @Parameter(description = "通知ID", example = "1") @PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();

        Notice notice = noticeService.getById(id);
        if (notice == null) {
            throw new BusinessException(404, "通知不存在");
        }

        // 验证是否是自己的通知
        if (!notice.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权删除该通知");
        }

        boolean success = noticeService.removeById(id);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "删除失败");
    }

    /**
     * 获取系统通知会话信息
     * @return 系统通知会话信息
     */
    @GetMapping("/session")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取系统通知会话", description = "获取系统通知会话信息（最后一条通知、未读数）")
    public ApiResponse<NoticeSessionVO> getNoticeSession() {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId())
                .orderByDesc(Notice::getCreateTime);

        List<Notice> notices = noticeService.list(queryWrapper);

        NoticeSessionVO vo = new NoticeSessionVO();
        vo.setSessionId("system");
        vo.setTitle("系统通知");
        vo.setIcon("/icons/system-notification.png");

        if (!notices.isEmpty()) {
            Notice lastNotice = notices.get(0);
            vo.setLastNoticeTitle(lastNotice.getTitle());
            vo.setLastNoticeTime(lastNotice.getCreateTime());

            long unreadCount = notices.stream()
                    .filter(n -> n.getIsRead() != null && n.getIsRead() == 0)
                    .count();
            vo.setUnreadCount((int) unreadCount);
        } else {
            vo.setLastNoticeTitle("暂无新通知");
            vo.setUnreadCount(0);
        }

        return ApiResponse.success(vo);
    }

    /**
     * 游标分页获取通知消息列表
     * @param lastId 上一页最后一条通知ID
     * @param size 每页数量
     * @return 通知列表
     */
    @GetMapping("/messages")
    @SaCheckLogin(type = "user")
    @Operation(summary = "游标分页获取通知", description = "使用游标分页获取通知列表")
    public ApiResponse<List<NoticeVO>> getNoticeMessagesByCursor(
            @Parameter(description = "上一页最后一条通知ID（首次不传）", example = "100") @RequestParam(name = "lastId", required = false) Long lastId,
            @Parameter(description = "每页数量", example = "20") @RequestParam(name = "size", defaultValue = "20") Integer size) {

        User currentUser = SaTokenUtil.getCurrentUser();

        // 限制每页最大数量
        size = Math.min(size, 50);

        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId());

        // 使用游标分页
        if (lastId != null && lastId > 0) {
            queryWrapper.lt(Notice::getId, lastId);
        }

        queryWrapper.orderByDesc(Notice::getId)
                .last("LIMIT " + size);

        List<Notice> notices = noticeService.list(queryWrapper);

        List<NoticeVO> voList = notices.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ApiResponse.success(voList);
    }

    /**
     * 标记系统通知会话已读
     * @return 操作结果
     */
    @PutMapping("/session/read")
    @SaCheckLogin(type = "user")
    @Operation(summary = "标记系统通知已读", description = "标记所有系统通知为已读")
    public ApiResponse<Void> markSessionAsRead() {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notice::getUserId, currentUser.getId())
                .eq(Notice::getIsRead, 0);

        Notice updateNotice = new Notice();
        updateNotice.setIsRead(1);

        noticeService.update(updateNotice, queryWrapper);
        return ApiResponse.success();
    }

    /**
     * 发送系统通知（管理员）
     * @param notice 通知信息
     * @return 发送结果
     */
    @PostMapping("/admin/send")
    @SaCheckRole(value = "admin", type = "admin")
    @Operation(summary = "发送系统通知", description = "管理员发送系统通知（管理员接口）")
    public ApiResponse<NoticeVO> sendSystemNotice(@RequestBody Notice notice) {
        if (notice.getUserId() == null) {
            throw new BusinessException(400, "接收用户ID不能为空");
        }
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "通知标题不能为空");
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "通知内容不能为空");
        }

        // 默认为系统公告类型
        if (notice.getType() == null) {
            notice.setType(3);
        }
        notice.setIsRead(0);

        boolean success = noticeService.save(notice);
        if (success) {
            NoticeVO vo = convertToVO(notice);

            // SSE推送通知给接收者
            MessagePushEvent event = MessagePushEvent.noticeEvent(
                    vo,
                    messagePushService.nextSequence(),
                    notice.getUserId()
            );
            messagePushService.pushToUser(notice.getUserId(), event);

            return ApiResponse.success(vo);
        }
        return ApiResponse.error(500, "发送失败");
    }

    /**
     * 转换为VO
     */
    private NoticeVO convertToVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setUserId(notice.getUserId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setType(notice.getType());
        vo.setIsRead(notice.getIsRead());
        vo.setCreateTime(notice.getCreateTime());

        // 设置类型描述
        String typeDesc = switch (notice.getType()) {
            case 1 -> "审核通知";
            case 2 -> "订单通知";
            case 3 -> "系统公告";
            default -> "其他";
        };
        vo.setTypeDesc(typeDesc);

        return vo;
    }
}
