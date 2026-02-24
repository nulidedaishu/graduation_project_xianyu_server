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
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 卖家ID
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long category_id;

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
    private Integer locked_stock;

    /**
     * 主图
     */
    @TableField(value = "main_image")
    private String main_image;

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
    private String audit_msg;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime create_time;

    /**
     * 
     */
    @TableField(value = "update_time")
    private LocalDateTime update_time;

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
        Product other = (Product) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getCategory_id() == null ? other.getCategory_id() == null : this.getCategory_id().equals(other.getCategory_id()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getPrice() == null ? other.getPrice() == null : this.getPrice().equals(other.getPrice()))
            && (this.getStock() == null ? other.getStock() == null : this.getStock().equals(other.getStock()))
            && (this.getLocked_stock() == null ? other.getLocked_stock() == null : this.getLocked_stock().equals(other.getLocked_stock()))
            && (this.getMain_image() == null ? other.getMain_image() == null : this.getMain_image().equals(other.getMain_image()))
            && (this.getLocation() == null ? other.getLocation() == null : this.getLocation().equals(other.getLocation()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getAudit_msg() == null ? other.getAudit_msg() == null : this.getAudit_msg().equals(other.getAudit_msg()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()))
            && (this.getUpdate_time() == null ? other.getUpdate_time() == null : this.getUpdate_time().equals(other.getUpdate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getCategory_id() == null) ? 0 : getCategory_id().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getPrice() == null) ? 0 : getPrice().hashCode());
        result = prime * result + ((getStock() == null) ? 0 : getStock().hashCode());
        result = prime * result + ((getLocked_stock() == null) ? 0 : getLocked_stock().hashCode());
        result = prime * result + ((getMain_image() == null) ? 0 : getMain_image().hashCode());
        result = prime * result + ((getLocation() == null) ? 0 : getLocation().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getAudit_msg() == null) ? 0 : getAudit_msg().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
        result = prime * result + ((getUpdate_time() == null) ? 0 : getUpdate_time().hashCode());
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
        sb.append(", category_id=").append(category_id);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", price=").append(price);
        sb.append(", stock=").append(stock);
        sb.append(", locked_stock=").append(locked_stock);
        sb.append(", main_image=").append(main_image);
        sb.append(", location=").append(location);
        sb.append(", status=").append(status);
        sb.append(", audit_msg=").append(audit_msg);
        sb.append(", create_time=").append(create_time);
        sb.append(", update_time=").append(update_time);
        sb.append("]");
        return sb.toString();
    }
}