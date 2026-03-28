package xyz.yaungyue.secondhand.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI配置请求DTO
 */
@Data
public class AIConfigRequest {

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    private String name;

    /**
     * 服务商: openai, anthropic, ollama等
     */
    @NotBlank(message = "服务商不能为空")
    private String provider;

    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    /**
     * API基础地址
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String model;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;
}
