package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Province;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_province(省份表)】的数据库操作Service
* @createDate 2026-03-03 18:13:07
*/
public interface ProvinceService extends IService<Province> {

    /**
     * 根据国家ID查询省份列表
     *
     * @param countryId 国家ID
     * @return 省份列表
     */
    List<Province> getByCountryId(Long countryId);
}
