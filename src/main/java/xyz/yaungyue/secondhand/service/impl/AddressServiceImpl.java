package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Address;
import xyz.yaungyue.secondhand.service.AddressService;
import xyz.yaungyue.secondhand.mapper.AddressMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_address(收货地址表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
    implements AddressService{

}




