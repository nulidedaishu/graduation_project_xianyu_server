package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.AIConfig;

/**
 * AI服务接口
 */
public interface AIService {

    /**
     * 根据商品标题生成描述
     *
     * @param title 商品标题
     * @return 生成的描述
     */
    String generateProductDescription(String title);

    /**
     * 通用聊天
     *
     * @param message 用户消息
     * @return AI回复
     */
    String chat(String message);

    /**
     * 测试AI配置是否可用
     *
     * @param config AI配置
     * @return 测试结果
     */
    boolean testConfig(AIConfig config);

    /**
     * 刷新AI配置缓存
     */
    void refreshConfig();
}
