package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.yaungyue.secondhand.constant.ProductStatus;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.mapper.ProductMapper;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductQueryRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductReviewRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.model.entity.Category;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.CategoryService;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryService categoryService;
    private final UserService userService;

    /**
     * 创建商品
     *
     * @param request 商品创建请求
     * @param userId  用户 ID
     * @return 创建后的商品信息
     * @throws BusinessException 当分类不存在或保存失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductCreateRequest request, Long userId) {
        // 1. 验证分类是否存在
        Category category = categoryService.getById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "商品分类不存在");
        }

        // 2. 创建商品实体
        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(request.getCategoryId());
        product.setTitle(request.getName()); // DTO中使用name，Entity使用title
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(1); // 默认库存为1
        product.setLockedStock(0);
        product.setMainImage(request.getImageUrls()); // 主图字段存储图片URL
        product.setLocation(request.getContactInfo()); // 使用联系方式作为位置信息
        product.setStatus(ProductStatus.PENDING); // 默认待审核状态

        // 3. 保存商品
        boolean saved = this.save(product);
        if (!saved) {
            throw new BusinessException(500, "商品发布失败");
        }

        log.info("商品发布成功，商品ID: {}, 用户ID: {}", product.getId(), userId);

        // 4. 转换为VO返回
        return convertToVO(product);
    }

    /**
     * 审核商品（管理员操作）
     *
     * @param productId 商品 ID
     * @param request   商品审核请求（包含审核状态和审核消息）
     * @param adminId   管理员 ID
     * @return 审核后的商品信息
     * @throws BusinessException 当商品不存在、状态不是待审核或更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO reviewProduct(Long productId, ProductReviewRequest request, Long adminId) {
        // 1. 查询商品
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 2. 验证商品状态是否为待审核
        if (product.getStatus() != ProductStatus.PENDING) {
            throw new BusinessException(400, "只能审核待审核状态的商品");
        }

        // 3. 更新商品状态
        int newStatus = request.status() == 1 ? ProductStatus.APPROVED : ProductStatus.REJECTED;
        product.setStatus(newStatus);
        product.setAuditMsg(request.auditMsg());

        boolean updated = this.updateById(product);
        if (!updated) {
            throw new BusinessException(500, "商品审核失败");
        }

        log.info("商品审核完成，商品ID: {}, 审核结果: {}, 管理员ID: {}",
                productId, newStatus == ProductStatus.APPROVED ? "通过" : "驳回", adminId);

        return convertToVO(product);
    }

    /**
     * 获取待审核商品列表（分页）
     *
     * @param page 页码
     * @param size 每页数量
     * @return 待审核商品的分页结果
     */
    public IPage<ProductVO> getPendingProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.PENDING)
                .orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 根据 ID 查询商品详情
     *
     * @param productId 商品 ID
     * @return 商品详细信息
     * @throws BusinessException 当商品不存在时抛出
     */
    public ProductVO getProductById(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return convertToVO(product);
    }

    /**
     * 查询最新上架的商品列表（分页）
     *
     * @param page 页码
     * @param size 每页数量
     * @return 已上架商品的分页结果，按创建时间倒序
     */
    public IPage<ProductVO> getLatestProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.APPROVED)
                .apply("stock - locked_stock > 0")
                .orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 条件搜索商品
     *
     * @param request 查询条件（包含状态、分类、关键词等）
     * @return 符合条件的商品分页结果
     */
    public IPage<ProductVO> searchProducts(ProductQueryRequest request) {
        Page<Product> pageParam = new Page<>(request.page(), request.size());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (request.status() != null) {
            wrapper.eq(Product::getStatus, request.status());
        } else {
            // 默认只查询已上架的商品
            wrapper.eq(Product::getStatus, ProductStatus.APPROVED);
        }

        // 分类筛选
        if (request.categoryId() != null) {
            wrapper.eq(Product::getCategoryId, request.categoryId());
        }

        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(request.keyword())) {
            wrapper.and(w -> w.like(Product::getTitle, request.keyword())
                    .or()
                    .like(Product::getDescription, request.keyword()));
        }

        // 只查询有可用库存的商品（stock - locked_stock > 0）
        wrapper.apply("stock - locked_stock > 0");

        // 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 根据分类查询商品
     *
     * @param categoryId 分类 ID
     * @param page       页码
     * @param size       每页数量
     * @return 指定分类下已上架商品的分页结果
     */
    public IPage<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, ProductStatus.APPROVED)
                .apply("stock - locked_stock > 0")
                .orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 查询用户发布的商品（分页）
     *
     * @param userId 用户 ID
     * @param page   页码
     * @param size   每页数量
     * @param status 商品状态（可选，不传则默认排除已删除商品）
     * @return 用户发布的商品分页结果
     */
    @Override
    public IPage<ProductVO> getProductsByUser(Long userId, Integer page, Integer size, Integer status) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId);

        // 如果指定了状态，则按状态筛选
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        } else {
            // 默认不显示已删除的商品
            wrapper.ne(Product::getStatus, ProductStatus.DELETED);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 下架商品（卖家操作）
     *
     * @param productId 商品 ID
     * @param userId    用户 ID
     * @return 下架后的商品信息
     * @throws BusinessException 当商品不存在、无权操作或商品状态不是已上架时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductVO offlineProduct(Long productId, Long userId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 验证是否是商品所有者
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此商品");
        }

        // 只能下架已上架的商品
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new BusinessException(400, "只能下架已上架的商品");
        }

        product.setStatus(ProductStatus.OFFLINE);
        this.updateById(product);

        return convertToVO(product);
    }

    /**
     * 上架商品（卖家操作，重新上架已下架商品）
     *
     * @param productId 商品 ID
     * @param userId    用户 ID
     * @return 上架后的商品信息
     * @throws BusinessException 当商品不存在、无权操作或当前状态不允许上架时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductVO onlineProduct(Long productId, Long userId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 验证是否是商品所有者
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此商品");
        }

        // 只能重新上架已下架或被驳回的商品（需要重新审核）
        if (product.getStatus() == ProductStatus.OFFLINE ||
                product.getStatus() == ProductStatus.REJECTED) {
            product.setStatus(ProductStatus.PENDING); // 重新提交审核
            product.setAuditMsg(null);
        } else {
            throw new BusinessException(400, "当前状态不允许上架操作");
        }

        this.updateById(product);

        return convertToVO(product);
    }

    /**
     * 获取推荐商品（随机选取已上架商品）
     *
     * @param page 页码
     * @param size 每页数量
     * @return 随机推荐的已上架商品分页结果
     */
    public IPage<ProductVO> getRecommendedProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.APPROVED)
                .apply("stock - locked_stock > 0");

        // 使用MySQL的RAND()函数随机排序
        wrapper.last("ORDER BY RAND()");

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 删除商品（软删除）
     *
     * @param productId 商品 ID
     * @param userId    用户 ID
     * @return 删除后的商品信息
     * @throws BusinessException 当商品不存在、无权操作或当前状态不允许删除时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductVO deleteProduct(Long productId, Long userId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 验证是否是商品所有者
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此商品");
        }

        // 检查商品状态是否允许删除
        // 只有待审核、审核驳回、已下架、已售出状态的商品可以删除
        if (product.getStatus() != ProductStatus.PENDING &&
                product.getStatus() != ProductStatus.REJECTED &&
                product.getStatus() != ProductStatus.OFFLINE &&
                product.getStatus() != ProductStatus.SOLD) {
            throw new BusinessException(400, "当前状态不允许删除商品");
        }

        // 软删除：将状态设置为已删除
        product.setStatus(ProductStatus.DELETED);
        boolean updated = this.updateById(product);

        if (!updated) {
            throw new BusinessException(500, "商品删除失败");
        }

        log.info("商品删除成功，商品ID: {}, 用户ID: {}", productId, userId);

        return convertToVO(product);
    }

    /**
     * 修改商品信息
     *
     * @param request 商品更新请求
     * @param userId  用户 ID
     * @return 更新后的商品信息
     * @throws BusinessException 当商品不存在、无权修改、分类不存在或当前状态不允许修改时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(ProductUpdateRequest request, Long userId) {
        // 1. 查询商品
        Product product = this.getById(request.getId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 2. 验证是否是商品所有者
        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此商品");
        }

        // 3. 验证商品状态是否允许修改
        // 只有待审核、审核驳回、已下架状态的商品可以修改
        if (product.getStatus() != ProductStatus.PENDING &&
                product.getStatus() != ProductStatus.APPROVED &&
                product.getStatus() != ProductStatus.REJECTED &&
                product.getStatus() != ProductStatus.OFFLINE) {
            throw new BusinessException(400, "当前状态不允许修改商品，只有待审核、已上架、审核驳回或已下架的商品可以修改");
        }

        // 4. 验证分类是否存在
        Category category = categoryService.getById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "商品分类不存在");
        }

        // 5. 更新商品信息
        product.setTitle(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        product.setMainImage(request.getImageUrls());
        product.setLocation(request.getContactInfo());

        // 6. 如果商品是被驳回状态，修改后重置为待审核状态
        if (product.getStatus() == ProductStatus.REJECTED) {
            product.setStatus(ProductStatus.PENDING);
            product.setAuditMsg(null);
            log.info("商品被驳回后修改，重新提交审核，商品ID: {}", request.getId());
        }

        // 7. 保存商品
        boolean updated = this.updateById(product);
        if (!updated) {
            throw new BusinessException(500, "商品修改失败");
        }

        log.info("商品修改成功，商品ID: {}, 用户ID: {}", request.getId(), userId);

        // 8. 转换为VO返回
        return convertToVO(product);
    }

    /**
     * 转换为 VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setCategoryId(product.getCategoryId());
        vo.setImageUrls(product.getMainImage());
        vo.setDetail(product.getDescription()); // 详情使用描述
        vo.setContactInfo(product.getLocation()); // 联系方式使用位置字段
        vo.setStatus(product.getStatus());
        vo.setStock(product.getStock()); // 设置库存
        vo.setLockedStock(product.getLockedStock()); // 设置锁定库存
        vo.setUserId(product.getUserId());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());

        // 查询分类名称
        Category category = categoryService.getById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 查询发布者昵称
        User user = userService.getById(product.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
        }

        return vo;
    }

    /**
     * 锁定商品库存（下单时使用）
     *
     * @param productId 商品 ID
     * @param quantity  要锁定的数量
     * @return 是否锁定成功
     * @apiNote 使用乐观锁机制防止并发问题，通过比较当前锁定库存值确保数据一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(Long productId, Integer quantity) {
        // 1. 查询商品
        Product product = this.getById(productId);
        if (product == null) {
            log.warn("锁定库存失败，商品不存在，productId={}", productId);
            return false;
        }

        // 2. 计算可用库存
        int lockedStock = product.getLockedStock() != null ? product.getLockedStock() : 0;
        int availableStock = product.getStock() - lockedStock;

        // 3. 检查库存是否充足
        if (availableStock < quantity) {
            log.warn("锁定库存失败，库存不足，productId={}, available={}, requested={}",
                    productId, availableStock, quantity);
            return false;
        }

        // 4. 使用 MyBatis-Plus 的 update 方法进行乐观锁更新
        // 更新条件：商品 ID 匹配 && 版本号等于预期值（防止并发）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, productId)
                .eq(Product::getVersion, product.getVersion()); // 乐观锁条件

        Product updateProduct = new Product();
        updateProduct.setLockedStock(lockedStock + quantity);

        boolean updated = this.update(updateProduct, wrapper);
        
        if (updated) {
            log.info("锁定库存成功，productId={}, quantity={}, newLockedStock={}",
                    productId, quantity, lockedStock + quantity);
        } else {
            log.error("锁定库存失败（可能被其他事务修改），productId={}", productId);
        }
        
        return updated;
    }

    /**
     * 释放商品库存（取消订单时使用）
     *
     * @param productId 商品 ID
     * @param quantity  要释放的数量
     * @return 是否释放成功
     * @apiNote 使用乐观锁机制防止并发问题，通过比较当前锁定库存值确保数据一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseStock(Long productId, Integer quantity) {
        // 1. 查询商品
        Product product = this.getById(productId);
        if (product == null) {
            log.warn("释放库存失败，商品不存在，productId={}", productId);
            return false;
        }

        // 2. 计算当前锁定库存
        int lockedStock = product.getLockedStock() != null ? product.getLockedStock() : 0;
        
        // 3. 验证锁定库存是否足够释放
        if (lockedStock < quantity) {
            log.warn("释放库存失败，锁定库存不足，productId={}, locked={}, release={}",
                    productId, lockedStock, quantity);
            return false;
        }

        // 4. 更新锁定库存
        // 使用乐观锁：确保 version 没有被其他事务修改
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, productId)
                .eq(Product::getVersion, product.getVersion()); // 乐观锁条件

        Product updateProduct = new Product();
        updateProduct.setLockedStock(lockedStock - quantity);

        boolean updated = this.update(updateProduct, wrapper);
        
        if (updated) {
            log.info("释放库存成功，productId={}, quantity={}, newLockedStock={}",
                    productId, quantity, lockedStock - quantity);
        } else {
            log.error("释放库存失败（可能被其他事务修改），productId={}", productId);
        }
        
        return updated;
    }

    /**
     * 确认扣减商品库存（支付成功后使用）
     *
     * @param productId 商品 ID
     * @param quantity  要扣减的数量
     * @return 是否扣减成功
     * @apiNote 同时扣减总库存和锁定库存，使用乐观锁机制防止并发问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDeductStock(Long productId, Integer quantity) {
        // 1. 查询商品
        Product product = this.getById(productId);
        if (product == null) {
            log.warn("扣减库存失败，商品不存在，productId={}", productId);
            return false;
        }

        // 2. 验证锁定库存是否足够（确保之前已经锁定）
        int lockedStock = product.getLockedStock() != null ? product.getLockedStock() : 0;
        if (lockedStock < quantity) {
            log.warn("扣减库存失败，锁定库存不足，productId={}, locked={}, deduct={}",
                    productId, lockedStock, quantity);
            return false;
        }

        // 3. 同时减少总库存和锁定库存
        // 使用乐观锁：确保 version 没有被其他事务修改
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, productId)
                .eq(Product::getVersion, product.getVersion()); // 乐观锁条件

        Product updateProduct = new Product();
        updateProduct.setStock(product.getStock() - quantity); // 扣减总库存
        updateProduct.setLockedStock(lockedStock - quantity); // 扣减锁定库存

        boolean updated = this.update(updateProduct, wrapper);
        
        if (updated) {
            log.info("确认扣减库存成功，productId={}, quantity={}, newStock={}, newLockedStock={}",
                    productId, quantity, product.getStock() - quantity, lockedStock - quantity);
        } else {
            log.error("确认扣减库存失败（可能被其他事务修改），productId={}", productId);
        }
        
        return updated;
    }
}
