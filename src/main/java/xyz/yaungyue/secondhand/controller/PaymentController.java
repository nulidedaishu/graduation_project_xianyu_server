package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.PaymentVO;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.PaymentService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

/**
 * 支付Controller
 *
 * @author yaung
 * @date 2026-02-26
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付宝支付
     * @param orderId 订单 ID
     * @return 支付信息
     */
    @PostMapping("/create")
    @SaCheckLogin
    @Operation(summary = "创建支付宝支付")
    public ApiResponse<PaymentVO> createPayment(@RequestParam Long orderId) {
        User currentUser = SaTokenUtil.getCurrentUser();
        PaymentVO paymentVO = paymentService.createAlipay(orderId, currentUser.getId());
        return ApiResponse.success(paymentVO);
    }

    /**
     * 支付宝异步回调通知（无需登录）
     * @param request HTTP 请求
     * @return 处理结果
     */
    @PostMapping("/alipay/notify")
    @Operation(summary = "支付宝异步回调通知（无需登录）")
    public String alipayNotify(HttpServletRequest request) {
        return paymentService.handleAlipayNotify(request);
    }

    /**
     * 支付宝同步回调（页面跳转）
     * @param request HTTP 请求
     * @return 回调结果
     */
    @GetMapping("/alipay/return")
    @Operation(summary = "支付宝同步回调（页面跳转）")
    public ApiResponse<String> alipayReturn(HttpServletRequest request) {
        // 同步回调通常只用于页面跳转展示
        // 实际支付结果以异步通知为准
        return ApiResponse.success("支付完成，请等待系统处理");
    }
}
