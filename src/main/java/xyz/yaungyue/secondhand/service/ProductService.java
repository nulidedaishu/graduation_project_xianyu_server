package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductReviewRequest;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yaung
* @description 针对表【bus_product(商品信息表)】的数据库操作Service
* @createDate 2026-02-12 17:21:41
*/
public interface ProductService extends IService<Product> {

    /**
     * 发布商品
     * @param request 商品创建请求
     * @param userId 用户ID
     * @return 商品VO
     */
    ProductVO createProduct(ProductCreateRequest request, Long userId);

    /**
     * 管理员审核商品
     * @param productId 商品ID
     * @param request 审核请求
     * @param adminId 管理员ID
     * @return 审核后的商品VO
     */
    ProductVO reviewProduct(Long productId, ProductReviewRequest request, Long adminId);

    /**
     * 根据状态查询商品列表
     * @param status 商品状态
     * @return 商品VO列表
     */
    java.util.List<ProductVO> getProductsByStatus(Integer status);
}
