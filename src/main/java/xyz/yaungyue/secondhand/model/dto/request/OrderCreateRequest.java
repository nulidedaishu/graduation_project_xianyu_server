package xyz.yaungyue.secondhand.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求DTO
 *
 * @author yaungyue
 * @date 2026-02-26
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    /**
     * 购物车ID列表（从购物车下单时使用）
     * 与items二选一
     */
    @Schema(description = "购物车ID列表", example = "[1, 2, 3]")
    private List<Long> cartIds;

    /**
     * 商品列表（直接购买时使用）
     * 与cartIds二选一
     */
    @Valid
    @Schema(description = "商品列表（直接购买时使用）")
    private List<OrderItemRequest> items;

    /**
     * 收货地址ID
     */
    @NotNull(message = "收货地址ID不能为空")
    @Schema(description = "收货地址ID", example = "1")
    private Long addressId;

    /**
     * 备注
     */
    @Schema(description = "订单备注", example = "请尽快发货")
    private String remark;
}
