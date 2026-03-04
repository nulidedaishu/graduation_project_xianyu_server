package xyz.yaungyue.secondhand.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.model.dto.response.PaymentVO;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.service.OrderService;
import xyz.yaungyue.secondhand.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付Service实现
 *
 * @author yaung
 * @date 2026-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderService orderService;

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")
    private String privateKey;

    @Value("${alipay.public-key}")
    private String publicKey;

    @Value("${alipay.server-url}")
    private String serverUrl;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Override
    public PaymentVO createAlipay(Long orderId, Long userId) {
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2. 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "无权支付此订单");
        }

        // 3. 验证订单状态
        if (order.getStatus() != 0) { // PENDING_PAYMENT
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR.getCode(), "订单状态不允许支付");
        }

        // 4. 创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                serverUrl,
                appId,
                privateKey,
                "json",
                "UTF-8",
                publicKey,
                "RSA2"
        );

        // 5. 创建支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        // 构建业务参数
        String bizContent = String.format(
                "{\"out_trade_no\":\"%s\","
                        + "\"total_amount\":\"%s\","
                        + "\"subject\":\"二手交易平台订单-%s\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                order.getOrderSn(),
                order.getTotalAmount().toString(),
                order.getOrderSn()
        );
        request.setBizContent(bizContent);

        try {
            // 6. 调用支付宝接口
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (!response.isSuccess()) {
                log.error("支付宝支付创建失败：{}", response.getMsg());
                throw new BusinessException(ErrorCode.PAYMENT_FAILED.getCode(), "支付创建失败：" + response.getMsg());
            }

            // 7. 构建返回VO
            PaymentVO vo = new PaymentVO();
            vo.setOrderId(order.getId());
            vo.setOrderSn(order.getOrderSn());
            vo.setAmount(order.getTotalAmount());
            vo.setPayFormHtml(response.getBody());
            vo.setExpireTime(order.getExpireTime());

            log.info("支付宝支付创建成功，orderId={}, orderSn={}", orderId, order.getOrderSn());

            return vo;

        } catch (AlipayApiException e) {
            log.error("支付宝支付创建异常", e);
            throw new BusinessException(ErrorCode.PAYMENT_FAILED.getCode(), "支付创建异常：" + e.getMessage());
        }
    }

    @Override
    public String handleAlipayNotify(HttpServletRequest request) {
        // 1. 获取支付宝回调参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("支付宝回调参数：{}", params);

        // 2. 验证签名
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    publicKey,
                    "UTF-8",
                    "RSA2"
            );

            if (!signVerified) {
                log.error("支付宝回调签名验证失败");
                return "fail";
            }

            // 3. 处理支付结果
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");

            log.info("支付宝回调 - 订单号：{}，交易号：{}，状态：{}，金额：{}",
                    outTradeNo, tradeNo, tradeStatus, totalAmount);

            // 4. 只有交易成功才处理
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 查询订单
                Order order = orderService.lambdaQuery()
                        .eq(Order::getOrderSn, outTradeNo)
                        .one();

                if (order == null) {
                    log.error("支付宝回调：订单不存在，orderSn={}", outTradeNo);
                    return "fail";
                }

                // 验证金额
                BigDecimal notifyAmount = new BigDecimal(totalAmount);
                if (notifyAmount.compareTo(order.getTotalAmount()) != 0) {
                    log.error("支付宝回调：金额不匹配，orderSn={}，通知金额={}，订单金额={}",
                            outTradeNo, totalAmount, order.getTotalAmount());
                    return "fail";
                }

                // 更新订单状态为已支付
                orderService.paySuccess(order.getId(), 1); // 1-支付宝

                log.info("支付宝回调处理成功，orderSn={}", outTradeNo);
            }

            return "success";

        } catch (AlipayApiException e) {
            log.error("支付宝回调处理异常", e);
            return "fail";
        }
    }

    @Override
    public boolean verifyAlipaySign(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params, publicKey, "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            log.error("支付宝签名验证异常", e);
            return false;
        }
    }
}
