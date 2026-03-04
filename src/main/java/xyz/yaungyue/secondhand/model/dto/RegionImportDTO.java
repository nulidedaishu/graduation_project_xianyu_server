package xyz.yaungyue.secondhand.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 行政区划导入DTO，直接对应pca.json文件结构
 *
 * JSON结构：
 * {
 *   "北京市": {
 *     "市辖区": ["东城区", "西城区", "..."]
 *   },
 *   "河北省": {
 *     "石家庄市": ["长安区", "桥西区", "..."],
 *     "唐山市": ["路南区", "路北区", "..."]
 *   },
 *   ...
 * }
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
public class RegionImportDTO {

    /**
     * 行政区划数据
     * 省份名称 -> 城市名称 -> 区县名称列表
     */
    private Map<String, Map<String, List<String>>> data;
}