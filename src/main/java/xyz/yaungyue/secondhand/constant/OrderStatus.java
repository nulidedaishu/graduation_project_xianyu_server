package xyz.yaungyue.secondhand.constant;

/**
 * 订单状态常量
 */
public class OrderStatus {

    /**
     * 待付款
     */
    public static final int PENDING_PAYMENT = 0;

    /**
     * 待发货
     */
    public static final int PENDING_SHIPMENT = 1;

    /**
     * 待收货
     */
    public static final int PENDING_RECEIPT = 2;

    /**
     * 待评价
     */
    public static final int PENDING_REVIEW = 3;

    /**
     * 已完成
     */
    public static final int COMPLETED = 4;

    /**
     * 已取消
     */
    public static final int CANCELLED = 5;

    /**
     * 已关闭（超时未付款）
     */
    public static final int CLOSED = 6;

    /**
     * 获取状态描述
     * @param status 状态码
     * @return 状态描述
     */
    public static String getDescription(int status) {
        return switch (status) {
            case PENDING_PAYMENT -> "待付款";
            case PENDING_SHIPMENT -> "待发货";
            case PENDING_RECEIPT -> "待收货";
            case PENDING_REVIEW -> "待评价";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case CLOSED -> "已关闭";
            default -> "未知状态";
        };
    }
}
