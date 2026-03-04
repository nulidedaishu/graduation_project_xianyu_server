package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.listener.ImportProgressListener;
import xyz.yaungyue.secondhand.model.dto.ImportResult;

/**
 * 行政区划数据导入服务接口
 *
 * @author yaungyue
 * @date 2026-03-03
 */
public interface RegionImportService {

    /**
     * 导入行政区划数据
     *
     * @param filePath JSON文件路径（支持classpath:前缀）
     * @return 导入结果
     */
    ImportResult importRegions(String filePath);

    /**
     * 导入行政区划数据（带进度监听）
     *
     * @param filePath JSON文件路径
     * @param listener 进度监听器
     * @return 导入结果
     */
    ImportResult importRegions(String filePath, ImportProgressListener listener);

    /**
     * 检查是否需要导入数据
     * 当数据库中没有任何省份数据时返回true
     *
     * @return true表示需要导入，false表示已有数据
     */
    boolean needImport();

    /**
     * 获取默认数据文件路径
     *
     * @return 默认数据文件路径
     */
    default String getDefaultFilePath() {
        return "classpath:pca.json";
    }
}