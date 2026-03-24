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
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.FileVO;
import xyz.yaungyue.secondhand.model.entity.File;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.FileService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 文件管理模块单元测试
 */
@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private User testUser;
    private File testImageFile;
    private File testVideoFile;
    private File testDocFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        testImageFile = new File();
        testImageFile.setId(1L);
        testImageFile.setFileName("product_image.jpg");
        testImageFile.setFileUrl("https://oss.example.com/images/product_image.jpg");
        testImageFile.setFileSize(1024 * 500L); // 500KB
        testImageFile.setOssKey("images/product_image.jpg");
        testImageFile.setFileType(1); // 图片
        testImageFile.setBizType(1);  // 商品
        testImageFile.setBizId(1L);
        testImageFile.setCreateTime(LocalDateTime.now());

        testVideoFile = new File();
        testVideoFile.setId(2L);
        testVideoFile.setFileName("product_video.mp4");
        testVideoFile.setFileUrl("https://oss.example.com/videos/product_video.mp4");
        testVideoFile.setFileSize(1024L * 1024 * 10); // 10MB
        testVideoFile.setOssKey("videos/product_video.mp4");
        testVideoFile.setFileType(2); // 视频
        testVideoFile.setBizType(1);  // 商品
        testVideoFile.setBizId(1L);
        testVideoFile.setCreateTime(LocalDateTime.now());

        testDocFile = new File();
        testDocFile.setId(3L);
        testDocFile.setFileName("document.pdf");
        testDocFile.setFileUrl("https://oss.example.com/docs/document.pdf");
        testDocFile.setFileSize(1024L * 1024 * 2); // 2MB
        testDocFile.setOssKey("docs/document.pdf");
        testDocFile.setFileType(7); // PDF
        testDocFile.setBizType(4);  // 订单
        testDocFile.setBizId(2L);
        testDocFile.setCreateTime(LocalDateTime.now());
    }

    @Test
    void getFiles_Success() {
        // Given
        Page<File> filePage = new Page<>();
        filePage.setCurrent(1);
        filePage.setSize(10);
        filePage.setTotal(3);
        filePage.setRecords(Arrays.asList(testImageFile, testVideoFile, testDocFile));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(filePage);

            // When
            ApiResponse<IPage<FileVO>> response = fileController.getFiles(1, 10, null);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(3, response.data().getRecords().size());
        }
    }

    @Test
    void getFiles_WithBizTypeFilter() {
        // Given
        Page<File> filePage = new Page<>();
        filePage.setCurrent(1);
        filePage.setSize(10);
        filePage.setTotal(2);
        filePage.setRecords(Arrays.asList(testImageFile, testVideoFile));

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(filePage);

            // When - 按商品类型筛选
            ApiResponse<IPage<FileVO>> response = fileController.getFiles(1, 10, 1);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(2, response.data().getRecords().size());
        }
    }

    @Test
    void getFiles_EmptyList() {
        // Given
        Page<File> emptyPage = new Page<>();
        emptyPage.setCurrent(1);
        emptyPage.setSize(10);
        emptyPage.setTotal(0);
        emptyPage.setRecords(Collections.emptyList());

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.page(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(emptyPage);

            // When
            ApiResponse<IPage<FileVO>> response = fileController.getFiles(1, 10, null);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertTrue(response.data().getRecords().isEmpty());
        }
    }

    @Test
    void getFileById_Success() {
        // Given
        Long fileId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(fileId)).thenReturn(testImageFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(fileId);

            // Then
            assertEquals(200, response.code());
            assertNotNull(response.data());
            assertEquals(1L, response.data().getId());
            assertEquals("product_image.jpg", response.data().getFileName());
            assertEquals("图片", response.data().getFileTypeDesc());
            assertEquals("商品", response.data().getBizTypeDesc());
            assertEquals("500.00KB", response.data().getFileSizeDisplay());
        }
    }

    @Test
    void getFileById_NotFound() {
        // Given
        Long fileId = 999L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(fileId)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileController.getFileById(fileId));
            assertEquals(404, exception.getCode());
            assertEquals("文件不存在", exception.getMessage());
        }
    }

    @Test
    void deleteFile_Success() {
        // Given
        Long fileId = 1L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(fileId)).thenReturn(testImageFile);
            when(fileService.removeById(fileId)).thenReturn(true);

            // When
            ApiResponse<Void> response = fileController.deleteFile(fileId);

            // Then
            assertEquals(200, response.code());
            verify(fileService).removeById(fileId);
        }
    }

    @Test
    void deleteFile_NotFound() {
        // Given
        Long fileId = 999L;

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(fileId)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileController.deleteFile(fileId));
            assertEquals(404, exception.getCode());
            assertEquals("文件不存在", exception.getMessage());
        }
    }

    @Test
    void convertToVO_FileTypeImage() {
        // Given
        File imageFile = new File();
        imageFile.setId(1L);
        imageFile.setFileName("test.jpg");
        imageFile.setFileType(1);
        imageFile.setBizType(1);
        imageFile.setFileSize(1024L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(imageFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("图片", response.data().getFileTypeDesc());
        }
    }

    @Test
    void convertToVO_FileTypeVideo() {
        // Given
        File videoFile = new File();
        videoFile.setId(1L);
        videoFile.setFileName("test.mp4");
        videoFile.setFileType(2);
        videoFile.setBizType(1);
        videoFile.setFileSize(1024L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(videoFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("视频", response.data().getFileTypeDesc());
        }
    }

    @Test
    void convertToVO_FileTypeAudio() {
        // Given
        File audioFile = new File();
        audioFile.setId(1L);
        audioFile.setFileName("test.mp3");
        audioFile.setFileType(3);
        audioFile.setBizType(1);
        audioFile.setFileSize(1024L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(audioFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("音频", response.data().getFileTypeDesc());
        }
    }

    @Test
    void convertToVO_BizTypeUser() {
        // Given
        File userFile = new File();
        userFile.setId(1L);
        userFile.setFileName("avatar.jpg");
        userFile.setFileType(1);
        userFile.setBizType(2);
        userFile.setFileSize(1024L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(userFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("用户", response.data().getBizTypeDesc());
        }
    }

    @Test
    void formatFileSize_Bytes() {
        // Given
        File smallFile = new File();
        smallFile.setId(1L);
        smallFile.setFileName("small.txt");
        smallFile.setFileType(4);
        smallFile.setBizType(1);
        smallFile.setFileSize(500L); // 500B

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(smallFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("500B", response.data().getFileSizeDisplay());
        }
    }

    @Test
    void formatFileSize_KB() {
        // Given - testImageFile 是 500KB
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(testImageFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("500.00KB", response.data().getFileSizeDisplay());
        }
    }

    @Test
    void formatFileSize_MB() {
        // Given - testDocFile 是 2MB
        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(testDocFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("2.00MB", response.data().getFileSizeDisplay());
        }
    }

    @Test
    void formatFileSize_GB() {
        // Given
        File largeFile = new File();
        largeFile.setId(1L);
        largeFile.setFileName("large.zip");
        largeFile.setFileType(8);
        largeFile.setBizType(1);
        largeFile.setFileSize(1024L * 1024 * 1024 * 2); // 2GB

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(largeFile);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("2.00GB", response.data().getFileSizeDisplay());
        }
    }

    @Test
    void convertToVO_NullFileType() {
        // Given
        File fileWithNullType = new File();
        fileWithNullType.setId(1L);
        fileWithNullType.setFileName("unknown.xyz");
        fileWithNullType.setFileType(null);
        fileWithNullType.setBizType(null);
        fileWithNullType.setFileSize(1024L);

        try (MockedStatic<SaTokenUtil> saTokenUtil = mockStatic(SaTokenUtil.class)) {
            saTokenUtil.when(SaTokenUtil::getCurrentUser).thenReturn(testUser);
            when(fileService.getById(1L)).thenReturn(fileWithNullType);

            // When
            ApiResponse<FileVO> response = fileController.getFileById(1L);

            // Then
            assertEquals("其他", response.data().getFileTypeDesc());
            assertEquals("其他", response.data().getBizTypeDesc());
        }
    }
}
