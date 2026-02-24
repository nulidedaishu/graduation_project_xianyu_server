package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件上传记录表
 * @TableName sys_file
 */
@TableName(value ="sys_file")
@Data
public class File {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    @TableField(value = "file_name")
    private String file_name;

    /**
     * 访问地址
     */
    @TableField(value = "file_url")
    private String file_url;

    /**
     * 文件大小(Byte)
     */
    @TableField(value = "file_size")
    private Long file_size;

    /**
     * OSS存储对应的Key
     */
    @TableField(value = "oss_key")
    private String oss_key;

    /**
     * 文件类型(1-image, 2-video, 3-audio, 4-doc, 5-ppt, 6-xls, 7-pdf, 8-zip)
     */
    @TableField(value = "file_type")
    private Integer file_type;

    /**
     * 业务类型(1-product, 2-user, 3-shop, 4-order, 5-logistics, 6-invoice, 7-coupon)
     */
    @TableField(value = "biz_type")
    private Integer biz_type;

    /**
     * 业务ID(关联业务表的ID)
     */
    @TableField(value = "biz_id")
    private Long biz_id;

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
        File other = (File) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFile_name() == null ? other.getFile_name() == null : this.getFile_name().equals(other.getFile_name()))
            && (this.getFile_url() == null ? other.getFile_url() == null : this.getFile_url().equals(other.getFile_url()))
            && (this.getFile_size() == null ? other.getFile_size() == null : this.getFile_size().equals(other.getFile_size()))
            && (this.getOss_key() == null ? other.getOss_key() == null : this.getOss_key().equals(other.getOss_key()))
            && (this.getFile_type() == null ? other.getFile_type() == null : this.getFile_type().equals(other.getFile_type()))
            && (this.getBiz_type() == null ? other.getBiz_type() == null : this.getBiz_type().equals(other.getBiz_type()))
            && (this.getBiz_id() == null ? other.getBiz_id() == null : this.getBiz_id().equals(other.getBiz_id()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFile_name() == null) ? 0 : getFile_name().hashCode());
        result = prime * result + ((getFile_url() == null) ? 0 : getFile_url().hashCode());
        result = prime * result + ((getFile_size() == null) ? 0 : getFile_size().hashCode());
        result = prime * result + ((getOss_key() == null) ? 0 : getOss_key().hashCode());
        result = prime * result + ((getFile_type() == null) ? 0 : getFile_type().hashCode());
        result = prime * result + ((getBiz_type() == null) ? 0 : getBiz_type().hashCode());
        result = prime * result + ((getBiz_id() == null) ? 0 : getBiz_id().hashCode());
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
        sb.append(", file_name=").append(file_name);
        sb.append(", file_url=").append(file_url);
        sb.append(", file_size=").append(file_size);
        sb.append(", oss_key=").append(oss_key);
        sb.append(", file_type=").append(file_type);
        sb.append(", biz_type=").append(biz_type);
        sb.append(", biz_id=").append(biz_id);
        sb.append(", create_time=").append(create_time);
        sb.append("]");
        return sb.toString();
    }
}