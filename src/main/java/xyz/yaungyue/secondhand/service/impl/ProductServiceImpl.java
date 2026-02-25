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

import java.math.BigDecimal;
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

    @Override
    public List<ProductVO> getProductsByStatus(Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, status)
                .orderByDesc(Product::getCreateTime);

        List<Product> products = this.list(wrapper);
        return products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取待审核商品列表（分页）
     *
     * @param page 页码
     * @return 分页结果
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
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品VO
     */
    public ProductVO getProductById(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return convertToVO(product);
    }


    /**
     * 查询上架的商品列表（分页）
     *
     * @param page 页码
     * @return 分页结果
     */
    public IPage<ProductVO> getApprovedProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.APPROVED)
                .orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 条件搜索商品
     *
     * @param request 查询条件
     * @return 分页结果
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

        // 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 根据分类查询商品
     *
     * @param categoryId 分类ID
     * @param page       页码
     * @return 分页结果
     */
    public IPage<ProductVO> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, ProductStatus.APPROVED)
                .orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 查询用户发布的商品（分页）
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @param status 商品状态（可选）
     * @return 分页结果
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
     * @param productId 商品ID
     * @param userId    用户ID
     * @return 更新后的商品
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
     * @param productId 商品ID
     * @param userId    用户ID
     * @return 更新后的商品
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
     * 更新商品库存（下单时锁定库存）
     *
     * @param productId 商品ID
     * @param quantity  数量（正数表示锁定，负数表示释放）
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLockedStock(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            return false;
        }

        // 检查可用库存
        int availableStock = product.getStock() - product.getLockedStock();
        if (quantity > 0 && availableStock < quantity) {
            throw new BusinessException(400, "商品库存不足");
        }

        // 更新锁定库存
        int newLockedStock = product.getLockedStock() + quantity;
        if (newLockedStock < 0) {
            newLockedStock = 0;
        }

        product.setLockedStock(newLockedStock);
        return this.updateById(product);
    }

    /**
     * 获取推荐商品（随机选取已上架商品）
     *
     * @param page 页码
     * @return 分页结果
     */
    public IPage<ProductVO> getRecommendedProducts(Integer page, Integer size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.APPROVED);

        // 使用MySQL的RAND()函数随机排序
        wrapper.last("ORDER BY RAND()");

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToVO);
    }

    /**
     * 删除商品（软删除）
     *
     * @param productId 商品ID
     * @param userId    用户ID
     * @return 更新后的商品
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
     * 修改商品
     *
     * @param request 商品更新请求
     * @param userId  用户ID
     * @return 更新后的商品VO
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
     * 转换为VO
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
}
