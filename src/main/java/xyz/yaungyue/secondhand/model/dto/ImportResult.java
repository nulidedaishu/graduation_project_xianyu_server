package xyz.yaungyue.secondhand.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 行政区划导入结果
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
public class ImportResult {

    /**
     * 导入的省份数量
     */
    private int provinceCount = 0;

    /**
     * 导入的城市数量
     */
    private int cityCount = 0;

    /**
     * 导入的区县数量
     */
    private int districtCount = 0;

    /**
     * 导入的总记录数
     */
    private int totalCount = 0;

    /**
     * 导入耗时（毫秒）
     */
    private long duration = 0;

    /**
     * 是否导入成功
     */
    private boolean success = false;

    /**
     * 导入结果消息
     */
    private String message = "";

    /**
     * 警告信息列表（如重复数据）
     */
    private List<String> warnings = new ArrayList<>();

    /**
     * 计算总记录数
     */
    public void calculateTotalCount() {
        this.totalCount = this.provinceCount + this.cityCount + this.districtCount;
    }

    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * 判断是否有警告
     */
    public boolean hasWarnings() {
        return this.warnings != null && !this.warnings.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("ImportResult{success=%s, provinces=%d, cities=%d, districts=%d, total=%d, duration=%dms, message='%s'}",
                success, provinceCount, cityCount, districtCount, totalCount, duration, message);
    }
}