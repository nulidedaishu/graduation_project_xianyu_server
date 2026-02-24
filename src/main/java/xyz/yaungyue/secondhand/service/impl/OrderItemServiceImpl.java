package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.OrderItem;
import xyz.yaungyue.secondhand.service.OrderItemService;
import xyz.yaungyue.secondhand.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_order_item(订单明细表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
    implements OrderItemService{

}




