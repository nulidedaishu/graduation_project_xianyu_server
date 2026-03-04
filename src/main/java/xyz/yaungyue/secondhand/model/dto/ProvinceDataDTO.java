package xyz.yaungyue.secondhand.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 省份数据DTO
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
public class ProvinceDataDTO {

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 城市列表
     */
    private List<CityDataDTO> cities;

    /**
     * 构造函数
     */
    public ProvinceDataDTO() {
    }

    /**
     * 构造函数
     */
    public ProvinceDataDTO(String provinceName, List<CityDataDTO> cities) {
        this.provinceName = provinceName;
        this.cities = cities;
    }

    /**
     * 获取城市数量
     */
    public int getCityCount() {
        return cities != null ? cities.size() : 0;
    }

    /**
     * 获取区县总数
     */
    public int getDistrictCount() {
        if (cities == null) {
            return 0;
        }
        return cities.stream()
                .mapToInt(city -> city.getDistricts() != null ? city.getDistricts().size() : 0)
                .sum();
    }
}