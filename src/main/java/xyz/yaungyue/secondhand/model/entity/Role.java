package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色表
 * @TableName sys_role
 */
@TableName(value ="sys_role")
@Data
public class Role {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String role_name;

    /**
     * 角色标识
     */
    @TableField(value = "role_code")
    private String role_code;

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
        Role other = (Role) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRole_name() == null ? other.getRole_name() == null : this.getRole_name().equals(other.getRole_name()))
            && (this.getRole_code() == null ? other.getRole_code() == null : this.getRole_code().equals(other.getRole_code()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRole_name() == null) ? 0 : getRole_name().hashCode());
        result = prime * result + ((getRole_code() == null) ? 0 : getRole_code().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", role_name=").append(role_name);
        sb.append(", role_code=").append(role_code);
        sb.append("]");
        return sb.toString();
    }
}