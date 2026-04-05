package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.request.OrderCreateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.OrderVO;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.OrderService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;

/**
 * 订单Controller
 *
 * @author yaung
 * @date 2026-02-26
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * @param request 订单创建请求
     * @return 订单信息
     */
    @PostMapping
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "创建订单")
    public ApiResponse<OrderVO> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        User currentUser = SaTokenUtil.getCurrentUser();
        OrderVO orderVO = orderService.createOrder(request, currentUser.getId());
        return ApiResponse.success(orderVO);
    }

    /**
     * 获取我的订单列表
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    @GetMapping
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "获取我的订单列表")
    public ApiResponse<List<OrderVO>> getMyOrders(
            @RequestParam(required = false) Integer status) {
        User currentUser = SaTokenUtil.getCurrentUser();
        List<OrderVO> list = orderService.getMyOrders(currentUser.getId(), status);
        return ApiResponse.success(list);
    }

    /**
     * 获取我卖出的订单列表
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    @GetMapping("/sold")
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "获取我卖出的订单列表")
    public ApiResponse<List<OrderVO>> getSoldOrders(
            @RequestParam(required = false) Integer status) {
        User currentUser = SaTokenUtil.getCurrentUser();
        List<OrderVO> list = orderService.getSoldOrders(currentUser.getId(), status);
        return ApiResponse.success(list);
    }

    /**
     * 获取订单详情
     * @param id 订单 ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "获取订单详情")
    public ApiResponse<OrderVO> getOrderDetail(@PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();
        OrderVO orderVO = orderService.getOrderDetail(id, currentUser.getId());
        return ApiResponse.success(orderVO);
    }

    /**
     * 取消订单
     * @param id 订单 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/cancel")
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "取消订单")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();
        orderService.cancelOrder(id, currentUser.getId());
        return ApiResponse.success();
    }

    /**
     * 卖家发货
     * @param id 订单 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/ship")
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "卖家发货")
    public ApiResponse<Void> shipOrder(@PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();
        orderService.shipOrder(id, currentUser.getId());
        return ApiResponse.success();
    }

    /**
     * 确认收货
     * @param id 订单 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/receive")
    @SaCheckPermission(value = "user:order:*", type = "user")
    @Operation(summary = "确认收货")
    public ApiResponse<Void> confirmReceive(@PathVariable Long id) {
        User currentUser = SaTokenUtil.getCurrentUser();
        orderService.confirmReceive(id, currentUser.getId());
        return ApiResponse.success();
    }
}
