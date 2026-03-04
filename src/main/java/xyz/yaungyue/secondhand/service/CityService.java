package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.City;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_city(城市表)】的数据库操作Service
* @createDate 2026-03-03 18:13:07
*/
public interface CityService extends IService<City> {

    /**
     * 根据省份ID查询城市列表
     *
     * @param provinceId 省份ID
     * @return 城市列表
     */
    List<City> getByProvinceId(Long provinceId);
}
