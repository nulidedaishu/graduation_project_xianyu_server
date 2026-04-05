package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 信用积分Controller
 *
 * @author yaung
 * @date 2026-03-20
 */
@Slf4j
@RestController
@RequestMapping("/api/credit-logs")
@RequiredArgsConstructor
@Tag(name = "信用积分管理", description = "信用积分记录相关接口")
public class CreditLogController {

    private final CreditLogService creditLogService;
    private final OrderService orderService;
    private final UserService userService;

    /**
     * 获取积分记录
     * @param page 页码
     * @param size 每页数量
     * @param type 类型 (income-收入, expense-支出)
     * @return 积分记录列表
     */
    @GetMapping
    @SaCheckPermission(value = "user:credit:*", type = "user")
    @Operation(summary = "获取积分记录", description = "获取当前用户的信用积分变动记录")
    public ApiResponse<IPage<CreditLogVO>> getCreditLogs(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Parameter(description = "类型(income-收入,expense-支出)", example = "income") @RequestParam(name = "type", required = false) String type) {

        User currentUser = SaTokenUtil.getCurrentUser();

        IPage<CreditLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CreditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditLog::getUserId, currentUser.getId());

        // 根据类型筛选
        if ("income".equals(type)) {
            queryWrapper.gt(CreditLog::getChangeValue, 0);
        } else if ("expense".equals(type)) {
            queryWrapper.lt(CreditLog::getChangeValue, 0);
        }

        queryWrapper.orderByDesc(CreditLog::getCreateTime);

        IPage<CreditLog> creditLogPage = creditLogService.page(pageParam, queryWrapper);

        List<CreditLogVO> voList = creditLogPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<CreditLogVO> voPage = new Page<>();
        voPage.setCurrent(creditLogPage.getCurrent());
        voPage.setSize(creditLogPage.getSize());
        voPage.setTotal(creditLogPage.getTotal());
        voPage.setPages(creditLogPage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取积分统计
     * @return 积分统计信息
     */
    @GetMapping("/statistics")
    @SaCheckPermission(value = "user:credit:*", type = "user")
    @Operation(summary = "获取积分统计", description = "获取当前用户的信用积分统计信息")
    public ApiResponse<CreditStatisticsVO> getStatistics() {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 重新查询用户获取最新积分
        User user = userService.getById(currentUser.getId());
        Integer currentCredit = user.getCreditScore() != null ? user.getCreditScore() : 0;

        // 计算总收入
        LambdaQueryWrapper<CreditLog> incomeWrapper = new LambdaQueryWrapper<>();
        incomeWrapper.eq(CreditLog::getUserId, currentUser.getId())
                .gt(CreditLog::getChangeValue, 0);
        List<CreditLog> incomeLogs = creditLogService.list(incomeWrapper);
        int totalIncome = incomeLogs.stream().mapToInt(CreditLog::getChangeValue).sum();

        // 计算总支出
        LambdaQueryWrapper<CreditLog> expenseWrapper = new LambdaQueryWrapper<>();
        expenseWrapper.eq(CreditLog::getUserId, currentUser.getId())
                .lt(CreditLog::getChangeValue, 0);
        List<CreditLog> expenseLogs = creditLogService.list(expenseWrapper);
        int totalExpense = Math.abs(expenseLogs.stream().mapToInt(CreditLog::getChangeValue).sum());

        // 统计交易完成奖励次数
        long transactionRewardCount = incomeLogs.stream()
                .filter(log -> log.getReason() != null && log.getReason().contains("交易完成"))
                .count();

        // 统计好评奖励次数
        long goodEvaluateCount = incomeLogs.stream()
                .filter(log -> log.getReason() != null && log.getReason().contains("好评"))
                .count();

        // 计算信用等级
        String creditLevel;
        String creditLevelDesc;
        if (currentCredit >= 200) {
            creditLevel = "极好";
            creditLevelDesc = "您的信用极好，享受所有权益和优先推荐";
        } else if (currentCredit >= 150) {
            creditLevel = "优秀";
            creditLevelDesc = "您的信用非常优秀，享受优先推荐权益";
        } else if (currentCredit >= 100) {
            creditLevel = "良好";
            creditLevelDesc = "您的信用良好，正常享受平台服务";
        } else if (currentCredit >= 50) {
            creditLevel = "一般";
            creditLevelDesc = "您的信用一般，建议多完成交易提升信用";
        } else {
            creditLevel = "较低";
            creditLevelDesc = "您的信用较低，部分功能可能受限";
        }

        CreditStatisticsVO vo = new CreditStatisticsVO();
        vo.setCurrentCredit(currentCredit);
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setTransactionRewardCount((int) transactionRewardCount);
        vo.setGoodEvaluateCount((int) goodEvaluateCount);
        vo.setCreditLevel(creditLevel);
        vo.setCreditLevelDesc(creditLevelDesc);

        return ApiResponse.success(vo);
    }

    /**
     * 转换为VO
     */
    private CreditLogVO convertToVO(CreditLog creditLog) {
        CreditLogVO vo = new CreditLogVO();
        vo.setId(creditLog.getId());
        vo.setUserId(creditLog.getUserId());
        vo.setOrderId(creditLog.getOrderId());
        vo.setChangeValue(creditLog.getChangeValue());
        vo.setReason(creditLog.getReason());
        vo.setCreateTime(creditLog.getCreateTime());

        // 设置带符号的显示值
        if (creditLog.getChangeValue() > 0) {
            vo.setChangeValueDisplay("+" + creditLog.getChangeValue());
        } else {
            vo.setChangeValueDisplay(String.valueOf(creditLog.getChangeValue()));
        }

        // 查询订单编号
        if (creditLog.getOrderId() != null) {
            Order order = orderService.getById(creditLog.getOrderId());
            if (order != null) {
                vo.setOrderSn(order.getOrderSn());
            }
        }

        return vo;
    }
}
