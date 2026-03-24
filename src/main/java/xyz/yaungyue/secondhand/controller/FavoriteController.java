package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.request.FavoriteCreateRequest;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.FavoriteVO;
import xyz.yaungyue.secondhand.model.entity.Favorite;
import xyz.yaungyue.secondhand.model.entity.Product;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.FavoriteService;
import xyz.yaungyue.secondhand.service.ProductService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏Controller
 *
 * @author yaung
 * @date 2026-03-20
 */
@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "收藏管理", description = "商品收藏相关接口")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ProductService productService;

    /**
     * 收藏商品
     * @param request 收藏请求
     * @return 收藏信息
     */
    @PostMapping
    @SaCheckLogin(type = "user")
    @Operation(summary = "收藏商品", description = "收藏指定商品")
    public ApiResponse<FavoriteVO> addFavorite(@RequestBody @Valid FavoriteCreateRequest request) {
        User currentUser = SaTokenUtil.getCurrentUser();

        // 检查商品是否存在
        Product product = productService.getById(request.getProductId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, currentUser.getId())
                .eq(Favorite::getProductId, request.getProductId());
        Favorite existingFavorite = favoriteService.getOne(queryWrapper);
        if (existingFavorite != null) {
            throw new BusinessException(400, "该商品已收藏");
        }

        // 创建收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(currentUser.getId());
        favorite.setProductId(request.getProductId());

        boolean success = favoriteService.save(favorite);
        if (success) {
            FavoriteVO vo = convertToVO(favorite, product);
            return ApiResponse.success(vo);
        }
        return ApiResponse.error(500, "收藏失败");
    }

    /**
     * 取消收藏
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/{productId}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "取消收藏", description = "取消收藏指定商品")
    public ApiResponse<Void> removeFavorite(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long productId) {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, currentUser.getId())
                .eq(Favorite::getProductId, productId);

        boolean success = favoriteService.remove(queryWrapper);
        if (success) {
            return ApiResponse.success();
        }
        return ApiResponse.error(404, "收藏记录不存在");
    }

    /**
     * 获取收藏列表
     * @param page 页码
     * @param size 每页数量
     * @return 收藏列表
     */
    @GetMapping
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取收藏列表", description = "获取当前用户的收藏列表")
    public ApiResponse<IPage<FavoriteVO>> getFavorites(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size) {
        User currentUser = SaTokenUtil.getCurrentUser();

        IPage<Favorite> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, currentUser.getId())
                .orderByDesc(Favorite::getCreateTime);

        IPage<Favorite> favoritePage = favoriteService.page(pageParam, queryWrapper);

        // 转换为VO
        List<FavoriteVO> voList = favoritePage.getRecords().stream()
                .map(favorite -> {
                    Product product = productService.getById(favorite.getProductId());
                    return convertToVO(favorite, product);
                })
                .collect(Collectors.toList());

        IPage<FavoriteVO> voPage = new Page<>();
        voPage.setCurrent(favoritePage.getCurrent());
        voPage.setSize(favoritePage.getSize());
        voPage.setTotal(favoritePage.getTotal());
        voPage.setPages(favoritePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 检查是否已收藏
     * @param productId 商品ID
     * @return 是否已收藏
     */
    @GetMapping("/check/{productId}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏指定商品")
    public ApiResponse<Boolean> checkFavorite(
            @Parameter(description = "商品ID", example = "1") @PathVariable Long productId) {
        User currentUser = SaTokenUtil.getCurrentUser();

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, currentUser.getId())
                .eq(Favorite::getProductId, productId);

        boolean isFavorite = favoriteService.count(queryWrapper) > 0;
        return ApiResponse.success(isFavorite);
    }

    /**
     * 转换为VO
     */
    private FavoriteVO convertToVO(Favorite favorite, Product product) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(favorite.getId());
        vo.setUserId(favorite.getUserId());
        vo.setProductId(favorite.getProductId());
        vo.setCreateTime(favorite.getCreateTime());

        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductImage(product.getMainImage());
            vo.setProductPrice(product.getPrice() != null ? product.getPrice().toString() : "0");
        }

        return vo;
    }
}
