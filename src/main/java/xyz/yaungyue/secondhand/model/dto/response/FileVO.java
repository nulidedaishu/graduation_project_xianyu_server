package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件视图对象
 */
@Data
@Schema(description = "文件信息")
public class FileVO {

    @Schema(description = "文件ID", example = "1")
    private Long id;

    @Schema(description = "原始文件名", example = "example.jpg")
    private String fileName;

    @Schema(description = "访问地址", example = "https://oss.example.com/file.jpg")
    private String fileUrl;

    @Schema(description = "文件大小(Byte)", example = "1024000")
    private Long fileSize;

    @Schema(description = "文件大小显示", example = "1000KB")
    private String fileSizeDisplay;

    @Schema(description = "OSS存储Key", example = "uploads/2025/03/example.jpg")
    private String ossKey;

    @Schema(description = "文件类型(1-image, 2-video, 3-audio, 4-doc, 5-ppt, 6-xls, 7-pdf, 8-zip)", example = "1")
    private Integer fileType;

    @Schema(description = "文件类型描述", example = "图片")
    private String fileTypeDesc;

    @Schema(description = "业务类型(1-product, 2-user, 3-shop, 4-order, 5-logistics, 6-invoice, 7-coupon，8-mainImage)", example = "1")
    private Integer bizType;

    @Schema(description = "业务类型描述", example = "商品")
    private String bizTypeDesc;

    @Schema(description = "业务ID", example = "1")
    private Long bizId;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;
}
