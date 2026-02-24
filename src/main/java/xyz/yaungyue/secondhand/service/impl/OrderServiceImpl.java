package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Order;
import xyz.yaungyue.secondhand.service.OrderService;
import xyz.yaungyue.secondhand.mapper.OrderMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_order(订单主表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{

}




