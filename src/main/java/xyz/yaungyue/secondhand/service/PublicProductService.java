package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.ProductQueryRequest;
import xyz.yaungyue.secondhand.model.dto.response.PageResponse;
import xyz.yaungyue.secondhand.model.entity.Product;

/**
 * 公共商品服务
 */
public interface PublicProductService {
    
    /**
     * 获取已上架商品列表（分页）
     */
    PageResponse<Product> getPublishedProducts(ProductQueryRequest request);
    
    /**
     * 获取商品详情（仅限已上架商品）
     */
    Product getProductDetail(Long productId);
    
    /**
     * 根据分类获取商品列表
     */
    PageResponse<Product> getProductsByCategory(Long categoryId, Integer page, Integer size);
    
    /**
     * 搜索商品
     */
    PageResponse<Product> searchProducts(String keyword, Integer page, Integer size);
}