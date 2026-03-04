package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 区县响应VO
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
@Schema(description = "区县信息")
public class DistrictVO {

    /**
     * 区县ID
     */
    @Schema(description = "区县ID", example = "440305")
    private Long id;

    /**
     * 所属城市ID
     */
    @Schema(description = "所属城市ID", example = "440300")
    private Long cityId;

    /**
     * 区县名称
     */
    @Schema(description = "区县名称", example = "南山区")
    private String name;
}