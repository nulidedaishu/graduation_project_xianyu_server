package xyz.yaungyue.secondhand.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import xyz.yaungyue.secondhand.listener.ImportProgressListener;
import xyz.yaungyue.secondhand.service.RegionImportService;

/**
 * 行政区划数据初始化配置
 * 应用启动时自动导入行政区划数据
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Slf4j
@Component
public class RegionDataInitializer implements ApplicationRunner {

    @Autowired
    private RegionImportService regionImportService;

    @Autowired(required = false)
    private ImportProgressListener importProgressListener;

    @Value("${region.import.enabled:true}")
    private boolean importEnabled;

    @Value("${region.import.file-path:classpath:pca.json}")
    private String filePath;

    @Override
    public void run(ApplicationArguments args) {
        if (!importEnabled) {
            log.info("行政区划数据导入已禁用 (region.import.enabled=false)");
            return;
        }

        log.info("检查行政区划数据初始化状态...");

        try {
            // 检查是否需要导入
            if (!regionImportService.needImport()) {
                log.info("行政区划数据已存在，跳过导入");
                return;
            }

            log.info("开始自动导入行政区划数据...");

            // 执行导入
            if (importProgressListener != null) {
                regionImportService.importRegions(filePath, importProgressListener);
            } else {
                regionImportService.importRegions(filePath);
            }

            log.info("行政区划数据自动导入完成");

        } catch (Exception e) {
            log.error("行政区划数据自动导入失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}