package xyz.yaungyue.secondhand.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import xyz.yaungyue.secondhand.mapper.AIConfigMapper;
import xyz.yaungyue.secondhand.model.entity.AIConfig;
import xyz.yaungyue.secondhand.service.AIService;

/**
 * AI服务实现类
 */
@Service
@Slf4j
public class AIServiceImpl implements AIService {

    private final AIConfigMapper aiConfigMapper;

    // 缓存的ChatClient
    private volatile ChatClient chatClient;

    // 当前配置的更新时间，用于判断是否需要重建client
    private volatile Long currentConfigTimestamp;

    public AIServiceImpl(AIConfigMapper aiConfigMapper) {
        this.aiConfigMapper = aiConfigMapper;
    }

    @Override
    public String generateProductDescription(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("商品标题不能为空");
        }

        String prompt = buildProductDescriptionPrompt(title);
        return chat(prompt);
    }

    @Override
    public String chat(String message) {
        ChatClient client = getOrCreateChatClient();
        if (client == null) {
            throw new RuntimeException("AI服务未配置或配置无效");
        }

        try {
            return client.prompt()
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    @Override
    public boolean testConfig(AIConfig config) {
        try {
            ChatClient testClient = createChatClient(config);
            String response = testClient.prompt()
                    .user("Hello")
                    .call()
                    .content();
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.error("AI配置测试失败", e);
            return false;
        }
    }

    @Override
    public void refreshConfig() {
        synchronized (this) {
            this.chatClient = null;
            this.currentConfigTimestamp = null;
            log.info("AI配置缓存已刷新");
        }
    }

    /**
     * 获取或创建ChatClient
     */
    private ChatClient getOrCreateChatClient() {
        AIConfig config = getEnabledConfig();
        if (config == null) {
            return null;
        }

        // 将LocalDateTime转为时间戳用于比较
        Long configTimestamp = config.getUpdateTime() != null
                ? config.getUpdateTime().toEpochSecond(java.time.ZoneOffset.UTC)
                : config.getId();

        // 检查是否需要重建client
        if (chatClient == null || !configTimestamp.equals(currentConfigTimestamp)) {
            synchronized (this) {
                // 双重检查
                if (chatClient == null || !configTimestamp.equals(currentConfigTimestamp)) {
                    this.chatClient = createChatClient(config);
                    this.currentConfigTimestamp = configTimestamp;
                    log.info("AI ChatClient已重建，配置ID: {}, provider: {}, model: {}",
                            config.getId(), config.getProvider(), config.getModel());
                }
            }
        }

        return chatClient;
    }

    /**
     * 获取启用的配置
     */
    private AIConfig getEnabledConfig() {
        // 先查默认配置
        AIConfig config = aiConfigMapper.selectDefaultConfig();
        if (config != null) {
            return config;
        }
        // 没有默认配置则取第一个启用的
        return aiConfigMapper.selectFirstEnabledConfig();
    }

    /**
     * 根据配置创建ChatClient
     */
    private ChatClient createChatClient(AIConfig config) {
        String provider = config.getProvider();
        if (provider == null) {
            provider = "openai";
        }

        return switch (provider.toLowerCase()) {
            case "openai" -> createOpenAiClient(config);
            default -> throw new IllegalArgumentException("不支持的AI服务商: " + provider);
        };
    }

    /**
     * 创建OpenAI客户端
     */
    private ChatClient createOpenAiClient(AIConfig config) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.openai.com";
        }

        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(baseUrl)
                .build();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(config.getModel() != null ? config.getModel() : "gpt-3.5-turbo");

        if (config.getTemperature() != null) {
            optionsBuilder.temperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            optionsBuilder.maxTokens(config.getMaxTokens());
        }

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();

        ChatClient.Builder clientBuilder = ChatClient.builder(chatModel);

        // 如果有系统提示词，设置系统提示
        if (config.getSystemPrompt() != null && !config.getSystemPrompt().trim().isEmpty()) {
            clientBuilder.defaultSystem(config.getSystemPrompt());
        }

        return clientBuilder.build();
    }

    /**
     * 构建商品描述生成提示词
     */
    private String buildProductDescriptionPrompt(String title) {
        return String.format(
                "请为以下二手商品生成一段吸引人的商品描述（100-200字）：\n\n" +
                        "商品标题：%s\n\n" +
                        "要求：\n" +
                        "1. 描述要真实、详细，突出商品特点\n" +
                        "2. 语气友好，像个人卖家\n" +
                        "3. 可以适当说明使用情况和成色\n" +
                        "4. 不要包含价格信息\n" +
                        "5. 直接返回描述内容，不要加标题或前缀",
                title
        );
    }
}
