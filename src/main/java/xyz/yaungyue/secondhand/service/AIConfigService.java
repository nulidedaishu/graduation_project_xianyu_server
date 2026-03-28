package xyz.yaungyue.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.yaungyue.secondhand.model.dto.request.AIConfigRequest;
import xyz.yaungyue.secondhand.model.entity.AIConfig;

import java.util.List;

/**
 * AI配置管理服务接口
 */
public interface AIConfigService extends IService<AIConfig> {

    /**
     * 创建配置
     */
    AIConfig createConfig(AIConfigRequest request);

    /**
     * 更新配置
     */
    AIConfig updateConfig(Long id, AIConfigRequest request);

    /**
     * 删除配置
     */
    void deleteConfig(Long id);

    /**
     * 获取配置详情
     */
    AIConfig getConfigById(Long id);

    /**
     * 获取所有配置
     */
    List<AIConfig> listAllConfigs();

    /**
     * 设置默认配置
     */
    void setDefaultConfig(Long id);

    /**
     * 启用/禁用配置
     */
    void toggleEnabled(Long id, Integer enabled);

    /**
     * 测试配置
     */
    boolean testConfig(Long id);

    /**
     * 测试新配置（未保存）
     */
    boolean testNewConfig(AIConfigRequest request);
}
