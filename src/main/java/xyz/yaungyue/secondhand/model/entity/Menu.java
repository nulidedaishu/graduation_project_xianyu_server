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
     * 菜单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父级ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 菜单/权限名称
     */
    @TableField(value = "menu_name")
    private String menuName;

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
}
