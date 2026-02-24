package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单明细表
 * @TableName bus_order_item
 */
@TableName(value ="bus_order_item")
@Data
public class OrderItem {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID
     */
    @TableField(value = "order_id")
    private Long order_id;

    /**
     * 商品ID
     */
    @TableField(value = "product_id")
    private Long product_id;

    /**
     * 商品快照名称
     */
    @TableField(value = "product_name")
    private String product_name;

    /**
     * 商品快照图片
     */
    @TableField(value = "product_image")
    private String product_image;

    /**
     * 成交快照价
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

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
        OrderItem other = (OrderItem) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOrder_id() == null ? other.getOrder_id() == null : this.getOrder_id().equals(other.getOrder_id()))
            && (this.getProduct_id() == null ? other.getProduct_id() == null : this.getProduct_id().equals(other.getProduct_id()))
            && (this.getProduct_name() == null ? other.getProduct_name() == null : this.getProduct_name().equals(other.getProduct_name()))
            && (this.getProduct_image() == null ? other.getProduct_image() == null : this.getProduct_image().equals(other.getProduct_image()))
            && (this.getPrice() == null ? other.getPrice() == null : this.getPrice().equals(other.getPrice()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOrder_id() == null) ? 0 : getOrder_id().hashCode());
        result = prime * result + ((getProduct_id() == null) ? 0 : getProduct_id().hashCode());
        result = prime * result + ((getProduct_name() == null) ? 0 : getProduct_name().hashCode());
        result = prime * result + ((getProduct_image() == null) ? 0 : getProduct_image().hashCode());
        result = prime * result + ((getPrice() == null) ? 0 : getPrice().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
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
        sb.append(", product_id=").append(product_id);
        sb.append(", product_name=").append(product_name);
        sb.append(", product_image=").append(product_image);
        sb.append(", price=").append(price);
        sb.append(", quantity=").append(quantity);
        sb.append("]");
        return sb.toString();
    }
}