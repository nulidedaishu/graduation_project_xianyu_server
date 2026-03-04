package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 地址更新请求DTO
 *
 * @author yaungyue
 * @date 2026-02-28
 */
@Data
@Schema(description = "地址更新请求")
public class AddressUpdateRequest {

    /**
     * 收货人
     */
    @NotBlank(message = "收货人不能为空")
    @Schema(description = "收货人", example = "张三")
    private String consignee;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    /**
     * 省份ID
     */
    @NotNull(message = "省份ID不能为空")
    @Schema(description = "省份ID", example = "1")
    private Long provinceId;

    /**
     * 城市ID
     */
    @NotNull(message = "城市ID不能为空")
    @Schema(description = "城市ID", example = "2")
    private Long cityId;

    /**
     * 区县ID
     */
    @NotNull(message = "区县ID不能为空")
    @Schema(description = "区县ID", example = "3")
    private Long districtId;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址", example = "科技园南区1号楼")
    private String detailAddress;

    /**
     * 是否默认地址(1-是, 0-否)
     */
    @NotNull(message = "是否默认地址不能为空")
    @Schema(description = "是否默认地址(1-是, 0-否)", example = "1")
    private Integer isDefault;
}