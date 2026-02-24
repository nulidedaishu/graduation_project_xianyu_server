package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.yaungyue.secondhand.model.entity.Favorite;
import xyz.yaungyue.secondhand.service.FavoriteService;
import xyz.yaungyue.secondhand.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

/**
* @author yaung
* @description 针对表【bus_favorite(收藏表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite>
    implements FavoriteService{

}




