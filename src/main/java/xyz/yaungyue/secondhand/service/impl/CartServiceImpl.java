package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Cart;
import xyz.yaungyue.secondhand.service.CartService;
import xyz.yaungyue.secondhand.mapper.CartMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_cart(购物车表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
    implements CartService{

}




