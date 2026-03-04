package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.City;
import xyz.yaungyue.secondhand.service.CityService;
import xyz.yaungyue.secondhand.mapper.CityMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_city(城市表)】的数据库操作Service实现
* @createDate 2026-03-03 18:13:07
*/
@Service
public class CityServiceImpl extends ServiceImpl<CityMapper, City>
    implements CityService{

    @Override
    public List<City> getByProvinceId(Long provinceId) {
        LambdaQueryWrapper<City> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(City::getProvinceId, provinceId);
        return list(queryWrapper);
    }
}




