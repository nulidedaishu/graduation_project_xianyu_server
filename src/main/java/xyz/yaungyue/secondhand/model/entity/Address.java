package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 收货地址表
 * @TableName bus_address
 */
@TableName(value ="bus_address")
@Data
public class Address {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 收货人
     */
    @TableField(value = "consignee")
    private String consignee;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 省
     */
    @TableField(value = "province")
    private String province;

    /**
     * 市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 区/县
     */
    @TableField(value = "district")
    private String district;

    /**
     * 详细地址
     */
    @TableField(value = "detail_address")
    private String detail_address;

    /**
     * 是否默认(1-是, 0-否)
     */
    @TableField(value = "is_default")
    private Integer is_default;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime create_time;

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
        Address other = (Address) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getConsignee() == null ? other.getConsignee() == null : this.getConsignee().equals(other.getConsignee()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getProvince() == null ? other.getProvince() == null : this.getProvince().equals(other.getProvince()))
            && (this.getCity() == null ? other.getCity() == null : this.getCity().equals(other.getCity()))
            && (this.getDistrict() == null ? other.getDistrict() == null : this.getDistrict().equals(other.getDistrict()))
            && (this.getDetail_address() == null ? other.getDetail_address() == null : this.getDetail_address().equals(other.getDetail_address()))
            && (this.getIs_default() == null ? other.getIs_default() == null : this.getIs_default().equals(other.getIs_default()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getConsignee() == null) ? 0 : getConsignee().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getProvince() == null) ? 0 : getProvince().hashCode());
        result = prime * result + ((getCity() == null) ? 0 : getCity().hashCode());
        result = prime * result + ((getDistrict() == null) ? 0 : getDistrict().hashCode());
        result = prime * result + ((getDetail_address() == null) ? 0 : getDetail_address().hashCode());
        result = prime * result + ((getIs_default() == null) ? 0 : getIs_default().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
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
        sb.append(", consignee=").append(consignee);
        sb.append(", phone=").append(phone);
        sb.append(", province=").append(province);
        sb.append(", city=").append(city);
        sb.append(", district=").append(district);
        sb.append(", detail_address=").append(detail_address);
        sb.append(", is_default=").append(is_default);
        sb.append(", create_time=").append(create_time);
        sb.append("]");
        return sb.toString();
    }
}