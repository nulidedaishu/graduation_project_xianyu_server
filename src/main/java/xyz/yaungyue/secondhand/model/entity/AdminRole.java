package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理员角色关联表
 * @TableName sys_admin_role
 */
@TableName(value ="sys_admin_role")
@Data
public class AdminRole {
    /**
     * 管理员ID
     */
    @TableId(value = "admin_id")
    private Long admin_id;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
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
        AdminRole other = (AdminRole) that;
        return (this.getAdmin_id() == null ? other.getAdmin_id() == null : this.getAdmin_id().equals(other.getAdmin_id()))
            && (this.getRole_id() == null ? other.getRole_id() == null : this.getRole_id().equals(other.getRole_id()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAdmin_id() == null) ? 0 : getAdmin_id().hashCode());
        result = prime * result + ((getRole_id() == null) ? 0 : getRole_id().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", admin_id=").append(admin_id);
        sb.append(", role_id=").append(role_id);
        sb.append("]");
        return sb.toString();
    }
}