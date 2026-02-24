package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 订单主表
 * @TableName bus_order
 */
@TableName(value ="bus_order")
@Data
public class Order {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号(唯一)
     */
    @TableField(value = "order_sn")
    private String order_sn;

    /**
     * 买家ID
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 订单总额
     */
    @TableField(value = "total_amount")
    private BigDecimal total_amount;

    /**
     * 状态(0-待付款, 1-待发货, 2-待收货, 3-待评价, 4-交易完成, 5-已取消, 6-超时关闭)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 支付方式(1-支付宝, 2-微信, 3-模拟支付)
     */
    @TableField(value = "pay_type")
    private Integer pay_type;

    /**
     * 支付时间
     */
    @TableField(value = "pay_time")
    private LocalDateTime pay_time;

    /**
     * 发货时间
     */
    @TableField(value = "delivery_time")
    private LocalDateTime delivery_time;

    /**
     * 收货时间
     */
    @TableField(value = "receive_time")
    private LocalDateTime receive_time;

    /**
     * 支付截止时间(用于超时关闭)
     */
    @TableField(value = "expire_time")
    private LocalDateTime expire_time;

    /**
     * 收货地址快照(存储下单时的地址)
     */
    @TableField(value = "address_snapshot")
    private Object address_snapshot;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime create_time;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Order other = (Order) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOrder_sn() == null ? other.getOrder_sn() == null : this.getOrder_sn().equals(other.getOrder_sn()))
            && (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getTotal_amount() == null ? other.getTotal_amount() == null : this.getTotal_amount().equals(other.getTotal_amount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getPay_type() == null ? other.getPay_type() == null : this.getPay_type().equals(other.getPay_type()))
            && (this.getPay_time() == null ? other.getPay_time() == null : this.getPay_time().equals(other.getPay_time()))
            && (this.getDelivery_time() == null ? other.getDelivery_time() == null : this.getDelivery_time().equals(other.getDelivery_time()))
            && (this.getReceive_time() == null ? other.getReceive_time() == null : this.getReceive_time().equals(other.getReceive_time()))
            && (this.getExpire_time() == null ? other.getExpire_time() == null : this.getExpire_time().equals(other.getExpire_time()))
            && (this.getAddress_snapshot() == null ? other.getAddress_snapshot() == null : this.getAddress_snapshot().equals(other.getAddress_snapshot()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOrder_sn() == null) ? 0 : getOrder_sn().hashCode());
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getTotal_amount() == null) ? 0 : getTotal_amount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getPay_type() == null) ? 0 : getPay_type().hashCode());
        result = prime * result + ((getPay_time() == null) ? 0 : getPay_time().hashCode());
        result = prime * result + ((getDelivery_time() == null) ? 0 : getDelivery_time().hashCode());
        result = prime * result + ((getReceive_time() == null) ? 0 : getReceive_time().hashCode());
        result = prime * result + ((getExpire_time() == null) ? 0 : getExpire_time().hashCode());
        result = prime * result + ((getAddress_snapshot() == null) ? 0 : getAddress_snapshot().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", order_sn=").append(order_sn);
        sb.append(", user_id=").append(user_id);
        sb.append(", total_amount=").append(total_amount);
        sb.append(", status=").append(status);
        sb.append(", pay_type=").append(pay_type);
        sb.append(", pay_time=").append(pay_time);
        sb.append(", delivery_time=").append(delivery_time);
        sb.append(", receive_time=").append(receive_time);
        sb.append(", expire_time=").append(expire_time);
        sb.append(", address_snapshot=").append(address_snapshot);
        sb.append(", create_time=").append(create_time);
        sb.append("]");
        return sb.toString();
    }
}