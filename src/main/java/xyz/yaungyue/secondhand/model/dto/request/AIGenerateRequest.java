package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI生成请求DTO
 */
@Data
public class AIGenerateRequest {

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
    private String title;
}
