package xyz.yaungyue.secondhand.model.entity;

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
    private Long adminId;

    /**
     * 角色ID
     */
    private Long roleId;
}
