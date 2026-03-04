package xyz.yaungyue.secondhand.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 城市数据DTO
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Data
public class CityDataDTO {

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区县列表
     */
    private List<String> districts;

    /**
     * 构造函数
     */
    public CityDataDTO() {
    }

    /**
     * 构造函数
     */
    public CityDataDTO(String cityName, List<String> districts) {
        this.cityName = cityName;
        this.districts = districts;
    }

    /**
     * 获取区县数量
     */
    public int getDistrictCount() {
        return districts != null ? districts.size() : 0;
    }
}