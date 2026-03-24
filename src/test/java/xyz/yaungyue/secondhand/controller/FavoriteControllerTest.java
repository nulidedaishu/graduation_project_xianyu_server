package xyz.yaungyue.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 收藏模块单元测试
 */
@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private FavoriteController favoriteController;

    private User testUser;
    private Product testProduct;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("iPhone 14 Pro");
        testProduct.setPrice(new BigDecimal("5999.00"));
        testProduct.setMainImage("https://example.com/image.jpg");

        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUserId(1L);
        testFavorite.setProductId(1L);
        testFavorite.setCreateTime(LocalDateTime.now());
    }

    @Test
    void addFavorite_Success() {
        // Given
        FavoriteCreateRequest request = new FavoriteCreateRequest();
        request.setProductId(1L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);

            when(productService.getById(1L)).thenReturn(testProduct);
            when(favoriteService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(favoriteService.save(any(Favorite.class))).thenReturn(true);

            // When
            ApiResponse<FavoriteVO> response = favoriteController.addFavorite(request);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1L, response.data().getProductId());
            assertEquals("iPhone 14 Pro", response.data().getProductTitle());
            verify(favoriteService).save(any(Favorite.class));
        }
    }

    @Test
    void addFavorite_ProductNotFound() {
        // Given
        FavoriteCreateRequest request = new FavoriteCreateRequest();
        request.setProductId(999L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(productService.getById(999L)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> favoriteController.addFavorite(request));
            assertEquals(404, exception.getCode());
            assertEquals("商品不存在", exception.getMessage());
        }
    }

    @Test
    void addFavorite_AlreadyExists() {
        // Given
        FavoriteCreateRequest request = new FavoriteCreateRequest();
        request.setProductId(1L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);

            when(productService.getById(1L)).thenReturn(testProduct);
            when(favoriteService.getOne(any(LambdaQueryWrapper.class))).thenReturn(testFavorite);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> favoriteController.addFavorite(request));
            assertEquals(400, exception.getCode());
            assertEquals("该商品已收藏", exception.getMessage());
        }
    }

    @Test
    void removeFavorite_Success() {
        // Given
        Long productId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(favoriteService.remove(any(LambdaQueryWrapper.class))).thenReturn(true);

            // When
            ApiResponse<Void> response = favoriteController.removeFavorite(productId);

            // Then
            assertEquals(200, response.code());
            verify(favoriteService).remove(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void removeFavorite_NotFound() {
        // Given
        Long productId = 999L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(favoriteService.remove(any(LambdaQueryWrapper.class))).thenReturn(false);

            // When
            ApiResponse<Void> response = favoriteController.removeFavorite(productId);

            // Then
            assertEquals(404, response.code());
            assertEquals("收藏记录不存在", response.message());
        }
    }

    @Test
    void getFavorites_Success() {
        // Given
        Page<Favorite> favoritePage = new Page<>();
        favoritePage.setCurrent(1);
        favoritePage.setSize(10);
        favoritePage.setTotal(2);
        favoritePage.setRecords(Arrays.asList(testFavorite));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(favoriteService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(favoritePage);
            when(productService.getById(1L)).thenReturn(testProduct);

            // When
            ApiResponse<IPage<FavoriteVO>> response = favoriteController.getFavorites(1, 10);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1, response.data().getRecords().size());
            assertEquals(2, response.data().getTotal());
        }
    }

    @Test
    void getFavorites_EmptyList() {
        // Given
        Page<Favorite> emptyPage = new Page<>();
        emptyPage.setCurrent(1);
        emptyPage.setSize(10);
        emptyPage.setTotal(0);
        emptyPage.setRecords(Collections.emptyList());

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(favoriteService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(emptyPage);

            // When
            ApiResponse<IPage<FavoriteVO>> response = favoriteController.getFavorites(1, 10);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertTrue(response.data().getRecords().isEmpty());
        }
    }

    @Test
    void checkFavorite_True() {
        // Given
        Long productId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(favoriteService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // When
            ApiResponse<Boolean> response = favoriteController.checkFavorite(productId);

            // Then
            assertEquals(200, response.code());
            assertTrue(response.data());
        }
    }

    @Test
    void checkFavorite_False() {
        // Given
        Long productId = 2L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(favoriteService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // When
            ApiResponse<Boolean> response = favoriteController.checkFavorite(productId);

            // Then
            assertEquals(200, response.code());
            assertFalse(response.data());
        }
    }
}
