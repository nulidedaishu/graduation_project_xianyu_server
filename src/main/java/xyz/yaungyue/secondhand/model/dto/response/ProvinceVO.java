package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 省份响应VO
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
@Schema(description = "省份信息")
public class ProvinceVO {

    /**
     * 省份ID
     */
    @Schema(description = "省份ID", example = "440000")
    private Long id;

    /**
     * 所属国家ID
     */
    @Schema(description = "所属国家ID", example = "1")
    private Long countryId;

    /**
     * 省份名称
     */
    @Schema(description = "省份名称", example = "广东省")
    private String name;
}