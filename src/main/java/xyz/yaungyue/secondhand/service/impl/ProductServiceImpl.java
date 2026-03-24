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
import xyz.yaungyue.secondhand.constant.SystemConstants;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.mapper.ProductMapper;
import xyz.yaungyue.secondhand.model.dto.request.ProductCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductQueryRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductReviewRequest;
import xyz.yaungyue.secondhand.model.dto.request.ProductUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ProductDetailVO;
import xyz.yaungyue.secondhand.model.dto.response.ProductListVO;
import xyz.yaungyue.secondhand.model.dto.response.ProductVO;
import xyz.yaungyue.secondhand.model.entity.Category;
import xyz.yaungyue.secondhand.model.entity.City;
import xyz.yaungyue.secondhand.model.entity.District;
import xyz.yaungyue.secondhand.model.entity.File;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.entity.Province;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.CategoryService;
import xyz.yaungyue.secondhand.service.CityService;
import xyz.yaungyue.secondhand.service.DistrictService;
import xyz.yaungyue.secondhand.service.FileService;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.service.ProvinceService;
import xyz.yaungyue.secondhand.service.UserService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    private final FileService fileService;
    private final DistrictService districtService;
    private final CityService cityService;
    private final ProvinceService provinceService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String RANDOM_PRODUCTS_CACHE_KEY = "random:products:";
    private static final long RANDOM_CACHE_EXPIRE_MINUTES = 10;

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
        product.setMainImage(request.getMainImageUrl()); // 主图字段存储主图 URL
        product.setFreight(request.getFreight()); // 运费
        product.setDistrictId(request.getDistrictId()); // 区位置 ID
        product.setStatus(ProductStatus.PENDING); // 默认待审核状态

        // 3. 保存商品
        boolean saved = this.save(product);
        if (!saved) {
            throw new BusinessException(500, "商品发布失败");
        }

        log.info("商品发布成功，商品 ID: {}, 用户 ID: {}", product.getId(), userId);

        // 4. 保存商品图片到 file 表
        saveProductImages(product.getId(), request.getMainImageUrl(), request.getOtherImageUrls());

        // 5. 转换为 VO 返回
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
    public ProductDetailVO getProductDetailById(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 查询该商品的所有图片（从 file 表）
        List<String> imageUrls = getProductImages(productId);

        // 转换为详情 VO
        return convertToDetailVO(product, imageUrls);
    }

    /**
     * 查询最新上架的商品列表（分页）
     *
     * @param page          页码
     * @param size          每页数量
     * @param excludeUserId 要排除的用户 ID（可为 null，为 null 时不排除任何用户）
     * @return 已上架商品的分页结果，按创建时间倒序
     */
    public IPage<ProductListVO> getLatestProducts(Integer page, Integer size, Long excludeUserId) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.APPROVED)
                .apply("stock - locked_stock > 0");

        // 排除指定用户的商品
        if (excludeUserId != null) {
            wrapper.ne(Product::getUserId, excludeUserId);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToListVO);
    }

    /**
     * 条件搜索商品
     *
     * @param request       查询条件（包含状态、分类、关键词等）
     * @param excludeUserId 要排除的用户 ID（可为 null，为 null 时不排除任何用户）
     * @return 符合条件的商品分页结果
     */
    public IPage<ProductListVO> searchProducts(ProductQueryRequest request, Long excludeUserId) {
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

        // 排除指定用户的商品
        if (excludeUserId != null) {
            wrapper.ne(Product::getUserId, excludeUserId);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToListVO);
    }

    /**
     * 根据分类查询商品
     *
     * @param categoryId    分类 ID
     * @param page          页码
     * @param size          每页数量
     * @param excludeUserId 要排除的用户 ID（可为 null，为 null 时不排除任何用户）
     * @return 指定分类下已上架商品的分页结果
     */
    public IPage<ProductListVO> getProductsByCategory(Long categoryId, Integer page, Integer size, Long excludeUserId) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, ProductStatus.APPROVED)
                .apply("stock - locked_stock > 0");

        // 排除指定用户的商品
        if (excludeUserId != null) {
            wrapper.ne(Product::getUserId, excludeUserId);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = this.page(pageParam, wrapper);
        return productPage.convert(this::convertToListVO);
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
    public IPage<ProductListVO> getProductsByUser(Long userId, Integer page, Integer size, Integer status) {
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
        return productPage.convert(this::convertToListVO);
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
        if (product.getStatus() == ProductStatus.PENDING || product.getStatus() == ProductStatus.APPROVED || product.getStatus() == ProductStatus.REJECTED) {
            product.setStatus(ProductStatus.OFFLINE);
        } else {
            throw new BusinessException(400, "该商品不能下架");
        }
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
     * <p>
     * 实现方案：使用 Redis 缓存随机排序的商品 ID 列表
     * - 每小时生成一个新的随机池，所有用户在这一小时内看到相同的随机顺序
     * - 分页时从缓存的 ID 列表中截取，保证跨页无重复
     * - 缓存过期时间 10 分钟，避免内存压力
     *
     * @param page          页码
     * @param size          每页数量
     * @param excludeUserId 要排除的用户 ID（可为 null，为 null 时不排除任何用户）
     * @return 随机推荐的已上架商品分页结果
     */
    public IPage<ProductListVO> getRecommendedProducts(Integer page, Integer size, Long excludeUserId) {
        // 1. 生成当前时间片（每小时一个池）
        long timeSlot = System.currentTimeMillis() / (1000 * 10);
        String cacheKey = RANDOM_PRODUCTS_CACHE_KEY + timeSlot;

        // 2. 尝试从 Redis 获取已排序的商品 ID 列表
        String cachedIdsJson = stringRedisTemplate.opsForValue().get(cacheKey);
        List<Long> productIds;

        if (cachedIdsJson == null) {
            // 3. 缓存未命中，从数据库查询所有符合条件的商品 ID
            log.info("随机商品缓存未命中，生成新的随机池，timeSlot: {}", timeSlot);
            productIds = generateRandomProductIds(excludeUserId);

            // 4. 存入 Redis，设置 10 分钟过期
            if (!productIds.isEmpty()) {
                try {
                    String idsJson = new ObjectMapper().writeValueAsString(productIds);
                    stringRedisTemplate.opsForValue().set(
                            cacheKey,
                            idsJson,
                            RANDOM_CACHE_EXPIRE_MINUTES,
                            TimeUnit.MINUTES
                    );
                } catch (Exception e) {
                    log.error("序列化商品 ID 列表失败", e);
                }
            }
        } else {
            // 5. 缓存命中，解析 ID 列表
            try {
                productIds = new ObjectMapper().readValue(cachedIdsJson, new TypeReference<List<Long>>() {
                });
            } catch (Exception e) {
                log.error("解析商品 ID 列表失败，重新生成", e);
                productIds = generateRandomProductIds(excludeUserId);
            }
        }

        // 6. 在内存中分页
        return paginateRandomProducts(productIds, page, size);
    }

    /**
     * 生成随机排序的商品 ID 列表
     *
     * @param excludeUserId 要排除的用户 ID（可为 null）
     * @return 随机排序的商品 ID 列表
     */
    private List<Long> generateRandomProductIds(Long excludeUserId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.APPROVED)
                .apply("stock - locked_stock > 0")
                .select(Product::getId);

        // 排除指定用户的商品
        if (excludeUserId != null) {
            wrapper.ne(Product::getUserId, excludeUserId);
        }

        List<Long> allIds = this.list(wrapper)
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // 随机打乱顺序
        Collections.shuffle(allIds);
        log.info("生成随机商品池，共 {} 个商品", allIds.size());

        return allIds;
    }

    /**
     * 对随机商品 ID 列表进行内存分页
     */
    private IPage<ProductListVO> paginateRandomProducts(List<Long> productIds, Integer page, Integer size) {
        Page<ProductListVO> resultPage = new Page<>(page, size);

        // 如果没有商品，返回空分页
        if (productIds == null || productIds.isEmpty()) {
            resultPage.setTotal(0);
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        // 计算分页边界
        int total = productIds.size();
        int fromIndex = (page - 1) * size;

        // 如果起始位置超出范围，返回空页
        if (fromIndex >= total) {
            resultPage.setTotal(total);
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        int toIndex = Math.min(fromIndex + size, total);
        List<Long> pageIds = productIds.subList(fromIndex, toIndex);

        // 根据 ID 查询商品详情（保持顺序）
        List<Product> products = new ArrayList<>();
        for (Long id : pageIds) {
            Product product = this.getById(id);
            if (product != null) {
                products.add(product);
            }
        }

        // 转换为 VO
        List<ProductListVO> records = products.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        resultPage.setTotal(total);
        resultPage.setRecords(records);

        return resultPage;
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
        product.setMainImage(request.getMainImageUrl());
        product.setFreight(request.getFreight());
        product.setDistrictId(request.getDistrictId());

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

        log.info("商品修改成功，商品 ID: {}, 用户 ID: {}", request.getId(), userId);

        // 8. 更新商品图片到 file 表
        updateProductImages(product.getId(), request.getMainImageUrl(), request.getOtherImageUrls());

        // 9. 转换为 VO 返回
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
        vo.setStock(product.getStock());
        vo.setUserId(product.getUserId());

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

        // 根据 districtId 查询城市名称（格式：江苏苏州）
        if (product.getDistrictId() != null) {
            District district = districtService.getById(product.getDistrictId());
            if (district != null && district.getCityId() != null) {
                City city = cityService.getById(district.getCityId());
                if (city != null && city.getProvinceId() != null) {
                    Province province = provinceService.getById(city.getProvinceId());
                    if (province != null) {
                        vo.setCity(province.getName() + city.getName());
                    }
                }
            }
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

    /**
     * 保存商品图片到 file 表
     *
     * @param productId      商品 ID
     * @param mainImageUrl   主图 URL
     * @param otherImageUrls 其他图片 URL 列表
     */
    private void saveProductImages(Long productId, String mainImageUrl, List<String> otherImageUrls) {
        List<File> files = new ArrayList<>();

        // 添加主图（排序为第一位）
        if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
            File mainFile = new File();
            mainFile.setFileName("main_image");
            mainFile.setFileUrl(mainImageUrl);
            mainFile.setFileType(1); // 图片类型
            mainFile.setBizType(SystemConstants.FILE_BIZ_TYPE_MAIN_IMAGE); // 商品主图业务类型
            mainFile.setBizId(productId);
            files.add(mainFile);
        }

        // 添加其他图片
        if (otherImageUrls != null && !otherImageUrls.isEmpty()) {
            for (int i = 0; i < otherImageUrls.size(); i++) {
                String imageUrl = otherImageUrls.get(i);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    File file = new File();
                    file.setFileName("image_" + (i + 1));
                    file.setFileUrl(imageUrl);
                    file.setFileType(1); // 图片类型
                    file.setBizType(SystemConstants.FILE_BIZ_TYPE_PRODUCT); // 商品信息业务类型
                    file.setBizId(productId);
                    files.add(file);
                }
            }
        }

        // 批量保存到数据库
        if (!files.isEmpty()) {
            fileService.saveBatch(files);
            log.info("保存商品图片成功，商品 ID: {}, 图片数量：{}", productId, files.size());
        }
    }

    /**
     * 更新商品图片到 file 表
     *
     * @param productId      商品 ID
     * @param mainImageUrl   主图 URL
     * @param otherImageUrls 其他图片 URL 列表
     */
    private void updateProductImages(Long productId, String mainImageUrl, List<String> otherImageUrls) {
        // 1. 删除该商品原有的所有图片记录
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getBizId, productId)
                .in(File::getBizType, List.of(
                        SystemConstants.FILE_BIZ_TYPE_PRODUCT,
                        SystemConstants.FILE_BIZ_TYPE_MAIN_IMAGE
                )); // 商品图片和主图业务类型
        fileService.remove(wrapper);

        // 2. 保存新的图片记录
        saveProductImages(productId, mainImageUrl, otherImageUrls);
    }

    /**
     * 获取商品的所有图片 URL 列表（从 file 表）
     *
     * @param productId 商品 ID
     * @return 图片 URL 列表
     */
    private List<String> getProductImages(Long productId) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getBizId, productId)
                .in(File::getBizType, List.of(
                        SystemConstants.FILE_BIZ_TYPE_PRODUCT,
                        SystemConstants.FILE_BIZ_TYPE_MAIN_IMAGE
                )) // 商品图片和主图业务类型
                .orderByAsc(File::getId); // 按 ID 升序排列

        List<File> files = fileService.list(wrapper);

        // 手动排序：主图（FILE_BIZ_TYPE_MAIN_IMAGE）排在最前面
        return files.stream()
                .sorted((f1, f2) -> {
                    boolean isMain1 = SystemConstants.FILE_BIZ_TYPE_MAIN_IMAGE.equals(f1.getBizType());
                    boolean isMain2 = SystemConstants.FILE_BIZ_TYPE_MAIN_IMAGE.equals(f2.getBizType());
                    if (isMain1 && !isMain2) {
                        return -1; // 主图排前面
                    } else if (!isMain1 && isMain2) {
                        return 1;
                    } else {
                        return 0; // 保持原有顺序
                    }
                })
                .map(File::getFileUrl)
                .collect(Collectors.toList());
    }

    /**
     * 转换为商品详情 VO
     */
    private ProductDetailVO convertToDetailVO(Product product, List<String> imageUrls) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setName(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setImageUrls(imageUrls);
        vo.setStatus(product.getStatus());
        vo.setUserId(product.getUserId());
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

        // 根据 districtId 查询城市名称（格式：江苏省苏州市姑苏区）
        if (product.getDistrictId() != null) {
            District district = districtService.getById(product.getDistrictId());
            if (district != null && district.getCityId() != null) {
                City city = cityService.getById(district.getCityId());
                if (city != null && city.getProvinceId() != null) {
                    Province province = provinceService.getById(city.getProvinceId());
                    vo.setProvince(province.getName() + " " + city.getName() + " " + district.getName());
                }
            }
        }

        return vo;
    }

    /**
     * 转换为商品列表 VO
     */
    private ProductListVO convertToListVO(Product product) {
        ProductListVO vo = new ProductListVO();
        vo.setId(product.getId());
        vo.setName(product.getTitle());
        vo.setPrice(product.getPrice());
        vo.setMainImageUrl(product.getMainImage());
        vo.setStatus(product.getStatus());

        // 查询发布者昵称
        User user = userService.getById(product.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
        }

        return vo;
    }
}
