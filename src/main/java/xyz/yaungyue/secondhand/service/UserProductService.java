package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.entity.Product;

/**
 * 用户商品服务
 */
public interface UserProductService {
    
    /**
     * 发布商品
     */
    Product createProduct(ProductCreateRequest request, Long userId);
    
    /**
     * 更新商品
     */
    Product updateProduct(Long productId, ProductCreateRequest request, Long userId);
    
    /**
     * 删除商品
     */
    boolean deleteProduct(Long productId, Long userId);
    
    /**
     * 获取用户发布的商品列表
     */
    Object getProductListByUser(Long userId, Integer page, Integer size);
}