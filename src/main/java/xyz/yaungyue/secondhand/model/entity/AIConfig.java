package xyz.yaungyue.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI大模型配置表
 */
@TableName(value = "sys_ai_config")
@Data
public class AIConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 服务商: openai, anthropic, ollama等
     */
    private String provider;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API基础地址，为空时使用默认地址
     */
    private String baseUrl;

    /**
     * 模型名称，如 gpt-4, gpt-3.5-turbo
     */
    private String model;

    /**
     * 温度参数，0-2之间，默认0.7
     */
    private Double temperature;

    /**
     * 最大token数，默认2000
     */
    private Integer maxTokens;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 是否启用(1-启用, 0-禁用)
     */
    private Integer enabled;

    /**
     * 是否默认配置(1-是, 0-否)
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
