package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductReviewRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductQueryRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

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
     * 修改商品
     * @param request 商品更新请求
     * @param userId 用户ID
     * @return 更新后的商品VO
     */
    ProductVO updateProduct(ProductUpdateRequest request, Long userId);

    /**
     * 管理员审核商品
     * @param productId 商品ID
     * @param request 审核请求
     * @param adminId 管理员ID
     * @return 审核后的商品VO
     */
    ProductVO reviewProduct(Long productId, ProductReviewRequest request, Long adminId);

    /**
     * 获取待审核商品列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<ProductVO> getPendingProducts(Integer page, Integer size);

    /**
     * 根据ID查询商品详情
     * @param productId 商品ID
     * @return 商品VO
     */
    ProductVO getProductById(Long productId);

    /**
     * 查询上架的商品列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<ProductVO> getLatestProducts(Integer page, Integer size);

    /**
     * 条件搜索商品
     * @param request 查询条件
     * @return 分页结果
     */
    IPage<ProductVO> searchProducts(ProductQueryRequest request);

    /**
     * 根据分类查询商品
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size);

    /**
     * 查询用户发布的商品（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param status 商品状态（可选）
     * @return 分页结果
     */
    IPage<ProductVO> getProductsByUser(Long userId, Integer page, Integer size, Integer status);

    /**
     * 下架商品（卖家操作）
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 更新后的商品
     */
    ProductVO offlineProduct(Long productId, Long userId);

    /**
     * 上架商品（卖家操作，重新上架已下架商品）
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 更新后的商品
     */
    ProductVO onlineProduct(Long productId, Long userId);

    
    /**
     * 获取推荐商品（随机选取已上架商品）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<ProductVO> getRecommendedProducts(Integer page, Integer size);

    /**
     * 删除商品（软删除）
     * @param productId 商品 ID
     * @param userId 用户 ID
     * @return 更新后的商品
     */
    ProductVO deleteProduct(Long productId, Long userId);

    /**
     * 锁定商品库存（下单预占）
     * @param productId 商品 ID
     * @param quantity 锁定数量
     * @return 是否成功
     */
    boolean lockStock(Long productId, Integer quantity);

    /**
     * 释放商品库存（取消订单或超时关闭）
     * @param productId 商品 ID
     * @param quantity 释放数量
     * @return 是否成功
     */
    boolean releaseStock(Long productId, Integer quantity);

    /**
     * 确认扣减库存（支付成功）
     * @param productId 商品 ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    boolean confirmDeductStock(Long productId, Integer quantity);
}