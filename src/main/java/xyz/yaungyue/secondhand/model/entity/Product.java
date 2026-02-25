package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品信息表
 * @TableName bus_product
 */
@TableName(value ="bus_product")
@Data
public class Product {
    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 卖家ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 商品标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 商品详述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 库存(默认1)
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 锁定库存(用于下单预占)
     */
    @TableField(value = "locked_stock")
    private Integer lockedStock;

    /**
     * 主图
     */
    @TableField(value = "main_image")
    private String mainImage;

    /**
     * 地理位置(同城交易)
     */
    @TableField(value = "location")
    private String location;

    /**
     * 状态(0-待审核, 1-上架, 2-审核驳回, 3-已下架, 4-已售出, 5-已删除)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审核反馈意见
     */
    @TableField(value = "audit_msg")
    private String auditMsg;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
