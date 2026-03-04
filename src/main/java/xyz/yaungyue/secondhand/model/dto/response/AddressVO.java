package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 地址响应VO
 *
 * @author yaungyue
 * @date 2026-02-28
 */
@Data
@Schema(description = "地址信息")
public class AddressVO {

    /**
     * 地址ID
     */
    @Schema(description = "地址ID", example = "1")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 收货人
     */
    @Schema(description = "收货人", example = "张三")
    private String consignee;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    /**
     * 省
     */
    @Schema(description = "省", example = "广东省")
    private String province;

    /**
     * 省份ID
     */
    @Schema(description = "省份ID", example = "440000")
    private Long provinceId;

    /**
     * 城市ID
     */
    @Schema(description = "城市ID", example = "440300")
    private Long cityId;

    /**
     * 区县ID
     */
    @Schema(description = "区县ID", example = "440305")
    private Long districtId;

    /**
     * 市
     */
    @Schema(description = "市", example = "深圳市")
    private String city;

    /**
     * 区/县
     */
    @Schema(description = "区/县", example = "南山区")
    private String district;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址", example = "科技园南区1号楼")
    private String detailAddress;

    /**
     * 是否默认地址(1-是, 0-否)
     */
    @Schema(description = "是否默认地址(1-是, 0-否)", example = "1")
    private Integer isDefault;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-02-28T10:00:00")
    private LocalDateTime createTime;

    /**
     * 完整地址
     */
    @Schema(description = "完整地址", example = "广东省深圳市南山区科技园南区1号楼")
    private String fullAddress;
}