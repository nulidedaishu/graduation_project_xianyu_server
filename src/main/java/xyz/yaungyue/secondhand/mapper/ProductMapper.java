package xyz.yaungyue.secondhand.mapper;

import xyz.yaungyue.secondhand.model.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author yaung
* @description 针对表【bus_product(商品信息表)】的数据库操作Mapper
* @createDate 2026-02-12 17:21:41
* @Entity xyz.yaungyue.secondhand.entity.Product
*/
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据分类ID统计商品数量
     * @param categoryId 分类ID
     * @return 商品数量
     */
    int selectCountByCategoryId(Long categoryId);

    /**
     * 根据状态查询商品列表
     * @param status 商品状态
     * @return 商品列表
     */
    java.util.List<Product> selectByStatus(Integer status);

}
