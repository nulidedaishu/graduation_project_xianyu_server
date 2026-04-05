package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.yaungyue.secondhand.model.dto.request.AIGenerateRequest;
import xyz.yaungyue.secondhand.model.dto.response.AIGenerateResponse;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.service.AIService;

/**
 * AI服务接口（供普通用户使用）
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 服务", description = "AI 生成商品描述等服务接口")
public class AIController {

    private final AIService aiService;

    /**
     * 根据商品标题生成描述
     */
    @PostMapping("/generate-description")
    @SaCheckPermission(value = "user:ai:generate", type = "user")
    @Operation(summary = "生成商品描述", description = "根据商品标题使用 AI 生成商品描述文案")
    public ApiResponse<AIGenerateResponse> generateProductDescription(@RequestBody AIGenerateRequest request) {
        String description = aiService.generateProductDescription(request.getTitle());
        AIGenerateResponse response = new AIGenerateResponse();
        response.setDescription(description);
        return ApiResponse.success(response);
    }
}
