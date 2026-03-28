package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.mapper.AIConfigMapper;
import xyz.yaungyue.secondhand.model.dto.request.AIConfigRequest;
import xyz.yaungyue.secondhand.model.entity.AIConfig;
import xyz.yaungyue.secondhand.service.AIConfigService;
import xyz.yaungyue.secondhand.service.AIService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI配置管理服务实现类
 */
@Service
@Slf4j
public class AIConfigServiceImpl extends ServiceImpl<AIConfigMapper, AIConfig> implements AIConfigService {

    private final AIConfigMapper aiConfigMapper;
    private final AIService aiService;

    public AIConfigServiceImpl(AIConfigMapper aiConfigMapper, AIService aiService) {
        this.aiConfigMapper = aiConfigMapper;
        this.aiService = aiService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AIConfig createConfig(AIConfigRequest request) {
        AIConfig config = new AIConfig();
        copyProperties(request, config);
        config.setIsDefault(0);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());

        // 如果是第一个启用的配置，设为默认
        if (config.getEnabled() == 1 && getDefaultConfig() == null) {
            config.setIsDefault(1);
        }

        aiConfigMapper.insert(config);

        // 如果创建的是启用的配置，刷新AI服务
        if (config.getEnabled() == 1) {
            aiService.refreshConfig();
        }

        log.info("创建AI配置成功: {}", config.getName());
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AIConfig updateConfig(Long id, AIConfigRequest request) {
        AIConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        copyProperties(request, config);
        config.setUpdateTime(LocalDateTime.now());
        aiConfigMapper.updateById(config);

        // 刷新AI服务配置
        aiService.refreshConfig();

        log.info("更新AI配置成功: {}", config.getName());
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        AIConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        // 如果是默认配置，不允许直接删除
        if (config.getIsDefault() == 1) {
            // 查找其他启用的配置设为默认
            AIConfig newDefault = lambdaQuery()
                    .eq(AIConfig::getEnabled, 1)
                    .ne(AIConfig::getId, id)
                    .orderByAsc(AIConfig::getId)
                    .one();

            if (newDefault != null) {
                newDefault.setIsDefault(1);
                aiConfigMapper.updateById(newDefault);
            }
        }

        aiConfigMapper.deleteById(id);
        aiService.refreshConfig();

        log.info("删除AI配置成功: {}", config.getName());
    }

    @Override
    public AIConfig getConfigById(Long id) {
        AIConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        return config;
    }

    @Override
    public List<AIConfig> listAllConfigs() {
        return lambdaQuery().orderByDesc(AIConfig::getCreateTime).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        AIConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        if (config.getEnabled() != 1) {
            throw new RuntimeException("禁用的配置不能设为默认");
        }

        // 清除所有默认配置
        lambdaUpdate().set(AIConfig::getIsDefault, 0).update();

        // 设置当前为默认
        config.setIsDefault(1);
        config.setUpdateTime(LocalDateTime.now());
        aiConfigMapper.updateById(config);

        // 刷新AI服务
        aiService.refreshConfig();

        log.info("设置默认AI配置: {}", config.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleEnabled(Long id, Integer enabled) {
        AIConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        config.setEnabled(enabled);
        config.setUpdateTime(LocalDateTime.now());

        // 如果禁用的是默认配置，清除默认标记
        if (enabled == 0 && config.getIsDefault() == 1) {
            config.setIsDefault(0);
            // 查找其他启用的配置设为默认
            AIConfig newDefault = lambdaQuery()
                    .eq(AIConfig::getEnabled, 1)
                    .ne(AIConfig::getId, id)
                    .orderByAsc(AIConfig::getId)
                    .one();
            if (newDefault != null) {
                newDefault.setIsDefault(1);
                aiConfigMapper.updateById(newDefault);
            }
        }

        // 如果启用且当前没有默认配置，设为默认
        if (enabled == 1 && getDefaultConfig() == null) {
            config.setIsDefault(1);
        }

        aiConfigMapper.updateById(config);
        aiService.refreshConfig();

        log.info("{}AI配置: {}", enabled == 1 ? "启用" : "禁用", config.getName());
    }

    @Override
    public boolean testConfig(Long id) {
        AIConfig config = getById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        return aiService.testConfig(config);
    }

    @Override
    public boolean testNewConfig(AIConfigRequest request) {
        AIConfig config = new AIConfig();
        copyProperties(request, config);
        return aiService.testConfig(config);
    }

    /**
     * 复制属性
     */
    private void copyProperties(AIConfigRequest request, AIConfig config) {
        config.setName(request.getName());
        config.setProvider(request.getProvider());
        config.setApiKey(request.getApiKey());
        config.setBaseUrl(request.getBaseUrl());
        config.setModel(request.getModel());
        config.setTemperature(request.getTemperature());
        config.setMaxTokens(request.getMaxTokens());
        config.setSystemPrompt(request.getSystemPrompt());
        config.setEnabled(request.getEnabled());
    }

    /**
     * 获取当前默认配置
     */
    private AIConfig getDefaultConfig() {
        return lambdaQuery()
                .eq(AIConfig::getIsDefault, 1)
                .eq(AIConfig::getEnabled, 1)
                .one();
    }
}
