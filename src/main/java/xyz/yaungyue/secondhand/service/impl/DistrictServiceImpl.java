package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.District;
import xyz.yaungyue.secondhand.service.DistrictService;
import xyz.yaungyue.secondhand.mapper.DistrictMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_district(区县表)】的数据库操作Service实现
* @createDate 2026-03-03 18:13:07
*/
@Service
public class DistrictServiceImpl extends ServiceImpl<DistrictMapper, District>
    implements DistrictService{

    @Override
    public List<District> getByCityId(Long cityId) {
        LambdaQueryWrapper<District> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(District::getCityId, cityId);
        return list(queryWrapper);
    }
}




