package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Province;
import xyz.yaungyue.secondhand.service.ProvinceService;
import xyz.yaungyue.secondhand.mapper.ProvinceMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_province(省份表)】的数据库操作Service实现
* @createDate 2026-03-03 18:13:07
*/
@Service
public class ProvinceServiceImpl extends ServiceImpl<ProvinceMapper, Province>
    implements ProvinceService{

    @Override
    public List<Province> getByCountryId(Long countryId) {
        LambdaQueryWrapper<Province> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Province::getCountryId, countryId);
        return list(queryWrapper);
    }
}




