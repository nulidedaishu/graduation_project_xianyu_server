package xyz.yaungyue.secondhand.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 城市响应VO
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
@Schema(description = "城市信息")
public class CityVO {

    /**
     * 城市ID
     */
    @Schema(description = "城市ID", example = "440300")
    private Long id;

    /**
     * 所属省份ID
     */
    @Schema(description = "所属省份ID", example = "440000")
    private Long provinceId;

    /**
     * 城市名称
     */
    @Schema(description = "城市名称", example = "深圳市")
    private String name;
}