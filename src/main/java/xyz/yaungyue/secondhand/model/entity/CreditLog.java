package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 信用积分流水表
 * @TableName bus_credit_log
 */
@TableName(value ="bus_credit_log")
@Data
public class CreditLog {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标用户ID
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 关联订单ID
     */
    @TableField(value = "order_id")
    private Long order_id;

    /**
     * 变动分值(可正可负)
     */
    @TableField(value = "change_value")
    private Integer change_value;

    /**
     * 变动原因
     */
    @TableField(value = "reason")
    private String reason;

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
        CreditLog other = (CreditLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getOrder_id() == null ? other.getOrder_id() == null : this.getOrder_id().equals(other.getOrder_id()))
            && (this.getChange_value() == null ? other.getChange_value() == null : this.getChange_value().equals(other.getChange_value()))
            && (this.getReason() == null ? other.getReason() == null : this.getReason().equals(other.getReason()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getOrder_id() == null) ? 0 : getOrder_id().hashCode());
        result = prime * result + ((getChange_value() == null) ? 0 : getChange_value().hashCode());
        result = prime * result + ((getReason() == null) ? 0 : getReason().hashCode());
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
        sb.append(", user_id=").append(user_id);
        sb.append(", order_id=").append(order_id);
        sb.append(", change_value=").append(change_value);
        sb.append(", reason=").append(reason);
        sb.append(", create_time=").append(create_time);
        sb.append("]");
        return sb.toString();
    }
}