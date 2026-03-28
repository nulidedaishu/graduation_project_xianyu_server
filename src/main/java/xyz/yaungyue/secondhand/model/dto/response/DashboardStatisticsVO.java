package xyz.yaungyue.secondhand.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台首页统计数据VO
 */
@Data
public class DashboardStatisticsVO {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 总商品数
     */
    private Long totalProducts;

    /**
     * 待审核商品数
     */
    private Long pendingProducts;

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 今日订单数
     */
    private Long todayOrders;

    /**
     * 总交易额
     */
    private BigDecimal totalAmount;

    /**
     * 今日交易额
     */
    private BigDecimal todayAmount;
}
