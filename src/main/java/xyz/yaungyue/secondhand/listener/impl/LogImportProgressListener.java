package xyz.yaungyue.secondhand.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.yaungyue.secondhand.listener.ImportProgressListener;
import xyz.yaungyue.secondhand.model.dto.ImportResult;

/**
 * 日志输出进度监听器实现
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Slf4j
@Component
public class LogImportProgressListener implements ImportProgressListener {

    @Override
    public void onImportStart(int totalProvinces) {
        log.info("开始导入行政区划数据，共 {} 个省份", totalProvinces);
    }

    @Override
    public void onProvinceProgress(int currentProvince, String provinceName, int totalProvinces) {
        int percent = (int) ((currentProvince * 100.0) / totalProvinces);
        log.info("导入进度: {}% ({}/{}) - 正在处理省份: {}", percent, currentProvince, totalProvinces, provinceName);
    }

    @Override
    public void onCityProgress(String provinceName, String cityName, int currentCity, int totalCities) {
        if (totalCities > 10 && currentCity % 10 == 0) {
            // 城市较多时，每10个城市输出一次日志
            int percent = (int) ((currentCity * 100.0) / totalCities);
            log.debug("省份 {}: 城市进度 {}% ({}/{}) - {}", provinceName, percent, currentCity, totalCities, cityName);
        }
    }

    @Override
    public void onDistrictProgress(String cityName, String districtName, int currentDistrict, int totalDistricts) {
        // 区县数量太多，只在调试模式下输出详细日志
        if (log.isDebugEnabled() && totalDistricts > 20 && currentDistrict % 20 == 0) {
            int percent = (int) ((currentDistrict * 100.0) / totalDistricts);
            log.debug("城市 {}: 区县进度 {}% ({}/{}) - {}", cityName, percent, currentDistrict, totalDistricts, districtName);
        }
    }

    @Override
    public void onImportComplete(ImportResult result) {
        log.info("行政区划数据导入完成: {}", result);
        if (result.hasWarnings()) {
            log.warn("导入过程中发现 {} 个警告:", result.getWarnings().size());
            for (String warning : result.getWarnings()) {
                log.warn("  - {}", warning);
            }
        }
    }

    @Override
    public void onImportError(Exception error) {
        log.error("行政区划数据导入失败", error);
    }
}