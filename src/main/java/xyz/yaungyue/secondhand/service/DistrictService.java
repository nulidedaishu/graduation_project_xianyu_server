package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.District;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_district(区县表)】的数据库操作Service
* @createDate 2026-03-03 18:13:07
*/
public interface DistrictService extends IService<District> {

    /**
     * 根据城市ID查询区县列表
     *
     * @param cityId 城市ID
     * @return 区县列表
     */
    List<District> getByCityId(Long cityId);
}
