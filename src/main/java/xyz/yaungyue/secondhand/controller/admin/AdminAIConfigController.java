package xyz.yaungyue.secondhand.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.request.AIConfigRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.entity.AIConfig;
import xyz.yaungyue.secondhand.service.AIConfigService;

import java.util.List;

/**
 * 管理员AI配置管理接口
 */
@RestController
@RequestMapping("/api/admin/ai-config")
@RequiredArgsConstructor
@SaCheckRole(value = "admin", type = "admin")
public class AdminAIConfigController {

    private final AIConfigService aiConfigService;

    /**
     * 获取所有AI配置
     */
    @GetMapping
    public ApiResponse<List<AIConfig>> listConfigs() {
        return ApiResponse.success(aiConfigService.listAllConfigs());
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AIConfig> getConfig(@PathVariable Long id) {
        return ApiResponse.success(aiConfigService.getConfigById(id));
    }

    /**
     * 创建配置
     */
    @PostMapping
    public ApiResponse<AIConfig> createConfig(@Valid @RequestBody AIConfigRequest request) {
        return ApiResponse.success(aiConfigService.createConfig(request));
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public ApiResponse<AIConfig> updateConfig(@PathVariable Long id, @Valid @RequestBody AIConfigRequest request) {
        return ApiResponse.success(aiConfigService.updateConfig(id, request));
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConfig(@PathVariable Long id) {
        aiConfigService.deleteConfig(id);
        return ApiResponse.success();
    }

    /**
     * 设置默认配置
     */
    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefaultConfig(@PathVariable Long id) {
        aiConfigService.setDefaultConfig(id);
        return ApiResponse.success();
    }

    /**
     * 启用/禁用配置
     */
    @PutMapping("/{id}/enabled")
    public ApiResponse<Void> toggleEnabled(@PathVariable Long id, @RequestParam Integer enabled) {
        aiConfigService.toggleEnabled(id, enabled);
        return ApiResponse.success();
    }

    /**
     * 测试已保存的配置
     */
    @PostMapping("/{id}/test")
    public ApiResponse<Boolean> testConfig(@PathVariable Long id) {
        return ApiResponse.success(aiConfigService.testConfig(id));
    }

    /**
     * 测试新配置（保存前）
     */
    @PostMapping("/test")
    public ApiResponse<Boolean> testNewConfig(@Valid @RequestBody AIConfigRequest request) {
        return ApiResponse.success(aiConfigService.testNewConfig(request));
    }
}
