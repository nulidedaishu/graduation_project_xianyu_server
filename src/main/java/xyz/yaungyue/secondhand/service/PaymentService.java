package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.response.PaymentVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 支付Service
 *
 * @author yaung
 * @date 2026-02-26
 */
public interface PaymentService {

    /**
     * 创建支付宝支付
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 支付信息VO
     */
    PaymentVO createAlipay(Long orderId, Long userId);

    /**
     * 处理支付宝异步回调
     *
     * @param request HTTP请求
     * @return 处理结果（"success"或"fail"）
     */
    String handleAlipayNotify(HttpServletRequest request);

    /**
     * 验证支付宝回调签名
     *
     * @param params 回调参数
     * @return 是否验证通过
     */
    boolean verifyAlipaySign(java.util.Map<String, String> params);
}
