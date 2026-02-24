package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限菜单表
 * @TableName sys_menu
 */
@TableName(value ="sys_menu")
@Data
public class Menu {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父级ID
     */
    @TableField(value = "parent_id")
    private Long parent_id;

    /**
     * 菜单/权限名称
     */
    @TableField(value = "menu_name")
    private String menu_name;

    /**
     * 权限标识(如 user:add)
     */
    @TableField(value = "permission")
    private String permission;

    /**
     * 路由地址
     */
    @TableField(value = "path")
    private String path;

    /**
     * 类型(0-目录, 1-菜单, 2-按钮)
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

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
        Menu other = (Menu) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getParent_id() == null ? other.getParent_id() == null : this.getParent_id().equals(other.getParent_id()))
            && (this.getMenu_name() == null ? other.getMenu_name() == null : this.getMenu_name().equals(other.getMenu_name()))
            && (this.getPermission() == null ? other.getPermission() == null : this.getPermission().equals(other.getPermission()))
            && (this.getPath() == null ? other.getPath() == null : this.getPath().equals(other.getPath()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getSort() == null ? other.getSort() == null : this.getSort().equals(other.getSort()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getParent_id() == null) ? 0 : getParent_id().hashCode());
        result = prime * result + ((getMenu_name() == null) ? 0 : getMenu_name().hashCode());
        result = prime * result + ((getPermission() == null) ? 0 : getPermission().hashCode());
        result = prime * result + ((getPath() == null) ? 0 : getPath().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getSort() == null) ? 0 : getSort().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", parent_id=").append(parent_id);
        sb.append(", menu_name=").append(menu_name);
        sb.append(", permission=").append(permission);
        sb.append(", path=").append(path);
        sb.append(", type=").append(type);
        sb.append(", sort=").append(sort);
        sb.append("]");
        return sb.toString();
    }
}