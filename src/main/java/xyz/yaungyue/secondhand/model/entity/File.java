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
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 访问地址
     */
    private String fileUrl;

    /**
     * 文件大小(Byte)
     */
    private Long fileSize;

    /**
     * OSS存储对应的Key
     */
    private String ossKey;

    /**
     * 文件类型(1-image, 2-video, 3-audio, 4-doc, 5-ppt, 6-xls, 7-pdf, 8-zip)
     */
    private Integer fileType;

    /**
     * 业务类型(1-product, 2-user, 3-shop, 4-order, 5-logistics, 6-invoice, 7-coupon，8-mainImage)
     */
    private Integer bizType;

    /**
     * 业务ID(关联业务表的ID)
     */
    private Long bizId;

    /**
     * 
     */
    private LocalDateTime createTime;

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
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileUrl() == null ? other.getFileUrl() == null : this.getFileUrl().equals(other.getFileUrl()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getOssKey() == null ? other.getOssKey() == null : this.getOssKey().equals(other.getOssKey()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getBizType() == null ? other.getBizType() == null : this.getBizType().equals(other.getBizType()))
            && (this.getBizId() == null ? other.getBizId() == null : this.getBizId().equals(other.getBizId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileUrl() == null) ? 0 : getFileUrl().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getOssKey() == null) ? 0 : getOssKey().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getBizType() == null) ? 0 : getBizType().hashCode());
        result = prime * result + ((getBizId() == null) ? 0 : getBizId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileUrl=").append(fileUrl);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", ossKey=").append(ossKey);
        sb.append(", fileType=").append(fileType);
        sb.append(", bizType=").append(bizType);
        sb.append(", bizId=").append(bizId);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}