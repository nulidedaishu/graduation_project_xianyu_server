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
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 卖家ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品详述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存(默认1)
     */
    private Integer stock;

    /**
     * 锁定库存(用于下单预占)
     */
    private Integer lockedStock;

    /**
     * 主图
     */
    private String mainImage;

    /**
     * 状态(0-待审核, 1-上架, 2-审核驳回, 3-已下架, 4-已售出, 5-已删除)
     */
    private Integer status;

    /**
     * 审核反馈意见
     */
    private String auditMsg;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

    /**
     * 版本号(用于乐观锁)
     */
    private Integer version;

    /**
     * 运费(默认0)
     */
    private BigDecimal freight;

    /**
     * 区位置(同城交易)
     */
    private Long districtId;

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
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getPrice() == null ? other.getPrice() == null : this.getPrice().equals(other.getPrice()))
            && (this.getStock() == null ? other.getStock() == null : this.getStock().equals(other.getStock()))
            && (this.getLockedStock() == null ? other.getLockedStock() == null : this.getLockedStock().equals(other.getLockedStock()))
            && (this.getMainImage() == null ? other.getMainImage() == null : this.getMainImage().equals(other.getMainImage()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getAuditMsg() == null ? other.getAuditMsg() == null : this.getAuditMsg().equals(other.getAuditMsg()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getFreight() == null ? other.getFreight() == null : this.getFreight().equals(other.getFreight()))
            && (this.getDistrictId() == null ? other.getDistrictId() == null : this.getDistrictId().equals(other.getDistrictId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getPrice() == null) ? 0 : getPrice().hashCode());
        result = prime * result + ((getStock() == null) ? 0 : getStock().hashCode());
        result = prime * result + ((getLockedStock() == null) ? 0 : getLockedStock().hashCode());
        result = prime * result + ((getMainImage() == null) ? 0 : getMainImage().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getAuditMsg() == null) ? 0 : getAuditMsg().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getFreight() == null) ? 0 : getFreight().hashCode());
        result = prime * result + ((getDistrictId() == null) ? 0 : getDistrictId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", price=").append(price);
        sb.append(", stock=").append(stock);
        sb.append(", lockedStock=").append(lockedStock);
        sb.append(", mainImage=").append(mainImage);
        sb.append(", status=").append(status);
        sb.append(", auditMsg=").append(auditMsg);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", version=").append(version);
        sb.append(", freight=").append(freight);
        sb.append(", districtId=").append(districtId);
        sb.append("]");
        return sb.toString();
    }
}