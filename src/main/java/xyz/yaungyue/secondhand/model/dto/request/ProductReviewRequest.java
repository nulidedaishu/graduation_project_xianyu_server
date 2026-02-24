package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 商品审核请求
 */
public record ProductReviewRequest(
    @NotNull(message = "商品ID不能为空")
    Long productId,
    
    @NotNull(message = "审核状态不能为空")
    Integer status, // 1-通过, 2-驳回
    
    String auditMsg // 驳回原因
) {}