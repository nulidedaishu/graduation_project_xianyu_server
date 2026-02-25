package xyz.yaungyue.secondhand.constant;

/**
 * 商品状态常量
 */
public class ProductStatus {

    /**
     * 待审核
     */
    public static final int PENDING = 0;

    /**
     * 已上架（审核通过）
     */
    public static final int APPROVED = 1;

    /**
     * 审核驳回
     */
    public static final int REJECTED = 2;

    /**
     * 已下架
     */
    public static final int OFFLINE = 3;

    /**
     * 已售出
     */
    public static final int SOLD = 4;

    /**
     * 已删除
     */
    public static final int DELETED = 5;

    /**
     * 获取状态描述
     * @param status 状态码
     * @return 状态描述
     */
    public static String getDescription(int status) {
        return switch (status) {
            case PENDING -> "待审核";
            case APPROVED -> "已上架";
            case REJECTED -> "审核驳回";
            case OFFLINE -> "已下架";
            case SOLD -> "已售出";
            case DELETED -> "已删除";
            default -> "未知状态";
        };
    }

    /**
     * 检查状态是否允许购买
     * @param status 状态码
     * @return 是否允许购买
     */
    public static boolean isAvailableForPurchase(int status) {
        return status == APPROVED;
    }

    /**
     * 检查状态是否允许编辑
     * @param status 状态码
     * @return 是否允许编辑
     */
    public static boolean isEditable(int status) {
        return status == PENDING || status == REJECTED || status == OFFLINE;
    }
}
