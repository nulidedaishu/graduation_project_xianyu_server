package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 聊天记录表
 * @TableName bus_chat_message
 */
@TableName(value ="bus_chat_message")
@Data
public class ChatMessage {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    @TableField(value = "sender_id")
    private Long sender_id;

    /**
     * 接收者ID
     */
    @TableField(value = "receiver_id")
    private Long receiver_id;

    /**
     * 关联商品ID(方便从商品发起咨询)
     */
    @TableField(value = "product_id")
    private Long product_id;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 消息类型(0-文字, 1-图片)
     */
    @TableField(value = "msg_type")
    private Integer msg_type;

    /**
     * 是否已读(0-未读, 1-已读)
     */
    @TableField(value = "is_read")
    private Integer is_read;

    /**
     * 会话标识(min_id_max_id)
     */
    @TableField(value = "session_key")
    private String session_key;

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
        ChatMessage other = (ChatMessage) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSender_id() == null ? other.getSender_id() == null : this.getSender_id().equals(other.getSender_id()))
            && (this.getReceiver_id() == null ? other.getReceiver_id() == null : this.getReceiver_id().equals(other.getReceiver_id()))
            && (this.getProduct_id() == null ? other.getProduct_id() == null : this.getProduct_id().equals(other.getProduct_id()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getMsg_type() == null ? other.getMsg_type() == null : this.getMsg_type().equals(other.getMsg_type()))
            && (this.getIs_read() == null ? other.getIs_read() == null : this.getIs_read().equals(other.getIs_read()))
            && (this.getSession_key() == null ? other.getSession_key() == null : this.getSession_key().equals(other.getSession_key()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSender_id() == null) ? 0 : getSender_id().hashCode());
        result = prime * result + ((getReceiver_id() == null) ? 0 : getReceiver_id().hashCode());
        result = prime * result + ((getProduct_id() == null) ? 0 : getProduct_id().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getMsg_type() == null) ? 0 : getMsg_type().hashCode());
        result = prime * result + ((getIs_read() == null) ? 0 : getIs_read().hashCode());
        result = prime * result + ((getSession_key() == null) ? 0 : getSession_key().hashCode());
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
        sb.append(", sender_id=").append(sender_id);
        sb.append(", receiver_id=").append(receiver_id);
        sb.append(", product_id=").append(product_id);
        sb.append(", content=").append(content);
        sb.append(", msg_type=").append(msg_type);
        sb.append(", is_read=").append(is_read);
        sb.append(", session_key=").append(session_key);
        sb.append(", create_time=").append(create_time);
        sb.append("]");
        return sb.toString();
    }
}