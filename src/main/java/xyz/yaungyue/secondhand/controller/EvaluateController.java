package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价Controller
 *
 * @author yaung
 * @date 2026-03-20
 */
@Slf4j
@RestController
@RequestMapping("/api/evaluates")
@RequiredArgsConstructor
@Tag(name = "评价管理", description = "订单评价相关接口")
public class EvaluateController {

    private final EvaluateService evaluateService;
    private final OrderService orderService;
    private final UserService userService;

    /**
     * 提交评价
     * @param request 评价请求
     * @return 评价信息
     */
    @PostMapping
    @SaCheckPermission(value = "user:evaluate:*", type = "user")
    @Operation(summary = "提交评价", description = "对订单进行评价（买家评卖家/卖家评买家）")
    public ApiResponse<EvaluateVO> submitEvaluate(@RequestBody @Valid EvaluateCreateRequest request) {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 检查订单是否存在
        Order order = orderService.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 验证评价类型和权限
        if (request.getType() == 1) {
            // 买家评卖家：当前用户必须是买家
            if (!currentUser.getId().equals(order.getUserId())) {
                throw new BusinessException(403, "只有买家可以评价卖家");
            }
            // 订单状态必须是待评价(3)或已完成(4)
            if (order.getStatus() != 3 && order.getStatus() != 4) {
                throw new BusinessException(400, "当前订单状态不可评价");
            }
        } else if (request.getType() == 2) {
            // 卖家评买家：需要验证当前用户是卖家
            // 这里简化处理，实际应该根据订单商品判断卖家
            throw new BusinessException(403, "功能暂未开放");
        } else {
            throw new BusinessException(400, "无效的评价类型");
        }

        // 检查是否已评价
        LambdaQueryWrapper<Evaluate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluate::getOrderId, request.getOrderId())
                .eq(Evaluate::getFromUserId, currentUser.getId())
                .eq(Evaluate::getType, request.getType());
        if (evaluateService.count(queryWrapper) > 0) {
            throw new BusinessException(400, "您已经评价过了");
        }

        // 创建评价
        Evaluate evaluate = new Evaluate();
        evaluate.setOrderId(request.getOrderId());
        evaluate.setFromUserId(currentUser.getId());
        evaluate.setToUserId(request.getToUserId());
        evaluate.setScore(request.getScore());
        evaluate.setContent(request.getContent());
        evaluate.setType(request.getType());

        boolean success = evaluateService.save(evaluate);
        if (success) {
            EvaluateVO vo = convertToVO(evaluate);
            return ApiResponse.success(vo);
        }
        return ApiResponse.error(500, "评价提交失败");
    }

    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页数量
     * @return 评价列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评价", description = "获取指定商品的评价列表")
    public ApiResponse<IPage<EvaluateVO>> getProductEvaluates(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long productId,
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size) {

        // 这里简化处理，实际应该通过订单关联查询商品的评价
        IPage<EvaluateVO> emptyPage = new Page<>();
        return ApiResponse.success(emptyPage);
    }

    /**
     * 获取用户信用评价
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 评价列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户信用评价", description = "获取指定用户的信用评价列表")
    public ApiResponse<IPage<EvaluateVO>> getUserEvaluates(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size) {

        IPage<Evaluate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Evaluate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluate::getToUserId, userId)
                .orderByDesc(Evaluate::getCreateTime);

        IPage<Evaluate> evaluatePage = evaluateService.page(pageParam, queryWrapper);

        List<EvaluateVO> voList = evaluatePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<EvaluateVO> voPage = new Page<>();
        voPage.setCurrent(evaluatePage.getCurrent());
        voPage.setSize(evaluatePage.getSize());
        voPage.setTotal(evaluatePage.getTotal());
        voPage.setPages(evaluatePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取我的评价
     * @param page 页码
     * @param size 每页数量
     * @return 评价列表
     */
    @GetMapping("/my")
    @SaCheckPermission(value = "user:evaluate:*", type = "user")
    @Operation(summary = "获取我的评价", description = "获取我发布的评价列表")
    public ApiResponse<IPage<EvaluateVO>> getMyEvaluates(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size) {

        User currentUser = SaTokenUtil.getCurrentUser();

        IPage<Evaluate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Evaluate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Evaluate::getFromUserId, currentUser.getId())
                .orderByDesc(Evaluate::getCreateTime);

        IPage<Evaluate> evaluatePage = evaluateService.page(pageParam, queryWrapper);

        List<EvaluateVO> voList = evaluatePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<EvaluateVO> voPage = new Page<>();
        voPage.setCurrent(evaluatePage.getCurrent());
        voPage.setSize(evaluatePage.getSize());
        voPage.setTotal(evaluatePage.getTotal());
        voPage.setPages(evaluatePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取待评价订单
     * @return 待评价订单列表
     */
    @GetMapping("/pending")
    @SaCheckPermission(value = "user:evaluate:*", type = "user")
    @Operation(summary = "获取待评价订单", description = "获取当前用户待评价的订单列表")
    public ApiResponse<List<PendingEvaluateOrderVO>> getPendingOrders() {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 查询待评价状态的订单 (status = 3)
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, currentUser.getId())
                .eq(Order::getStatus, 3)
                .orderByDesc(Order::getCreateTime);

        List<Order> orders = orderService.list(queryWrapper);

        // 这里简化处理，实际应该构建更完整的VO
        List<PendingEvaluateOrderVO> voList = new ArrayList<>();
        for (Order order : orders) {
            PendingEvaluateOrderVO vo = new PendingEvaluateOrderVO();
            vo.setOrderId(order.getId());
            vo.setOrderSn(order.getOrderSn());
            vo.setStatus(order.getStatus());
            vo.setStatusDesc("待评价");
            vo.setEvaluateType(1); // 买家评卖家
            vo.setCreateTime(order.getCreateTime());
            vo.setReceiveTime(order.getReceiveTime());
            voList.add(vo);
        }

        return ApiResponse.success(voList);
    }

    /**
     * 转换为VO
     */
    private EvaluateVO convertToVO(Evaluate evaluate) {
        EvaluateVO vo = new EvaluateVO();
        vo.setId(evaluate.getId());
        vo.setOrderId(evaluate.getOrderId());
        vo.setFromUserId(evaluate.getFromUserId());
        vo.setToUserId(evaluate.getToUserId());
        vo.setScore(evaluate.getScore());
        vo.setContent(evaluate.getContent());
        vo.setType(evaluate.getType());
        vo.setCreateTime(evaluate.getCreateTime());

        // 查询用户信息
        User fromUser = userService.getById(evaluate.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserNickname(fromUser.getNickname());
            vo.setFromUserAvatar(fromUser.getAvatar());
        }

        User toUser = userService.getById(evaluate.getToUserId());
        if (toUser != null) {
            vo.setToUserNickname(toUser.getNickname());
        }

        return vo;
    }
}
