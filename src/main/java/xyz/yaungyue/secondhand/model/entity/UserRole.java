package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联表
 * @TableName sys_user_role
 */
@TableName(value ="sys_user_role")
@Data
public class UserRole {
    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    private Long user_id;

    /**
     * 角色ID
     */
    private Long role_id;

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
        UserRole other = (UserRole) that;
        return (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getRole_id() == null ? other.getRole_id() == null : this.getRole_id().equals(other.getRole_id()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getRole_id() == null) ? 0 : getRole_id().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", user_id=").append(user_id);
        sb.append(", role_id=").append(role_id);
        sb.append("]");
        return sb.toString();
    }
}