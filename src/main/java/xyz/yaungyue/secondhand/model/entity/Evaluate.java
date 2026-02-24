package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评价表
 * @TableName bus_evaluate
 */
@TableName(value ="bus_evaluate")
@Data
public class Evaluate {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单
     */
    @TableField(value = "order_id")
    private Long order_id;

    /**
     * 评价发起人
     */
    @TableField(value = "from_user_id")
    private Long from_user_id;

    /**
     * 被评价人
     */
    @TableField(value = "to_user_id")
    private Long to_user_id;

    /**
     * 评分(1-5)
     */
    @TableField(value = "score")
    private Integer score;

    /**
     * 评价内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 类型(1-买家评卖家, 2-卖家评买家)
     */
    @TableField(value = "type")
    private Integer type;

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
        Evaluate other = (Evaluate) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOrder_id() == null ? other.getOrder_id() == null : this.getOrder_id().equals(other.getOrder_id()))
            && (this.getFrom_user_id() == null ? other.getFrom_user_id() == null : this.getFrom_user_id().equals(other.getFrom_user_id()))
            && (this.getTo_user_id() == null ? other.getTo_user_id() == null : this.getTo_user_id().equals(other.getTo_user_id()))
            && (this.getScore() == null ? other.getScore() == null : this.getScore().equals(other.getScore()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOrder_id() == null) ? 0 : getOrder_id().hashCode());
        result = prime * result + ((getFrom_user_id() == null) ? 0 : getFrom_user_id().hashCode());
        result = prime * result + ((getTo_user_id() == null) ? 0 : getTo_user_id().hashCode());
        result = prime * result + ((getScore() == null) ? 0 : getScore().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
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
        sb.append(", order_id=").append(order_id);
        sb.append(", from_user_id=").append(from_user_id);
        sb.append(", to_user_id=").append(to_user_id);
        sb.append(", score=").append(score);
        sb.append(", content=").append(content);
        sb.append(", type=").append(type);
        sb.append(", create_time=").append(create_time);
        sb.append("]");
        return sb.toString();
    }
}