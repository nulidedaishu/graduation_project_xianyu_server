package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.FileVO;
import xyz.yaungyue.secondhand.model.entity.File;
import xyz.yaungyue.secondhand.model.entity.User;
import xyz.yaungyue.secondhand.service.FileService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件管理Controller
 *
 * @author yaung
 * @date 2026-03-20
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传记录管理相关接口")
public class FileController {

    private final FileService fileService;

    /**
     * 获取文件列表
     * @param page 页码
     * @param size 每页数量
     * @param bizType 业务类型筛选
     * @return 文件列表
     */
    @GetMapping
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取文件列表", description = "获取当前用户上传的文件列表")
    public ApiResponse<IPage<FileVO>> getFiles(
            @Parameter(description = "页码", example = "1") @RequestParam(name = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(name = "size", defaultValue = "10") Integer size,
            @Parameter(description = "业务类型(1-product, 2-user, 3-shop, 4-order, 5-logistics, 6-invoice, 7-coupon，8-mainImage)", example = "1")
            @RequestParam(name = "bizType", required = false) Integer bizType) {

        User currentUser = SaTokenUtil.getCurrentUser();

        IPage<File> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();

        // 目前file表没有userId字段，暂时通过bizType和bizId关联查询
        // 实际业务中可能需要增加userId字段来区分文件所有者
        if (bizType != null) {
            queryWrapper.eq(File::getBizType, bizType);
        }

        queryWrapper.orderByDesc(File::getCreateTime);

        IPage<File> filePage = fileService.page(pageParam, queryWrapper);

        List<FileVO> voList = filePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        IPage<FileVO> voPage = new Page<>();
        voPage.setCurrent(filePage.getCurrent());
        voPage.setSize(filePage.getSize());
        voPage.setTotal(filePage.getTotal());
        voPage.setPages(filePage.getPages());
        voPage.setRecords(voList);

        return ApiResponse.success(voPage);
    }

    /**
     * 获取文件详情
     * @param id 文件ID
     * @return 文件详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "获取文件详情", description = "获取指定文件的详细信息")
    public ApiResponse<FileVO> getFileById(
            @Parameter(description = "文件ID", example = "1") @PathVariable Long id) {
        File file = fileService.getById(id);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }
        return ApiResponse.success(convertToVO(file));
    }

    /**
     * 删除文件
     * @param id 文件ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "user")
    @Operation(summary = "删除文件", description = "删除指定的文件记录（实际OSS文件删除需额外处理）")
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "文件ID", example = "1") @PathVariable Long id) {
        File file = fileService.getById(id);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }

        // TODO: 调用OSS服务删除实际文件
        // 这里仅删除数据库记录

        boolean success = fileService.removeById(id);
        if (success) {
            log.info("删除文件记录成功，fileId: {}, ossKey: {}", id, file.getOssKey());
            return ApiResponse.success();
        }
        return ApiResponse.error(500, "删除失败");
    }

    /**
     * 转换为VO
     */
    private FileVO convertToVO(File file) {
        FileVO vo = new FileVO();
        vo.setId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setFileUrl(file.getFileUrl());
        vo.setFileSize(file.getFileSize());
        vo.setOssKey(file.getOssKey());
        vo.setFileType(file.getFileType());
        vo.setBizType(file.getBizType());
        vo.setBizId(file.getBizId());
        vo.setCreateTime(file.getCreateTime());

        // 文件大小格式化
        if (file.getFileSize() != null) {
            vo.setFileSizeDisplay(formatFileSize(file.getFileSize()));
        }

        // 文件类型描述
        String fileTypeDesc = switch (file.getFileType() != null ? file.getFileType() : 0) {
            case 1 -> "图片";
            case 2 -> "视频";
            case 3 -> "音频";
            case 4 -> "文档";
            case 5 -> "演示文稿";
            case 6 -> "表格";
            case 7 -> "PDF";
            case 8 -> "压缩包";
            default -> "其他";
        };
        vo.setFileTypeDesc(fileTypeDesc);

        // 业务类型描述
        String bizTypeDesc = switch (file.getBizType() != null ? file.getBizType() : 0) {
            case 1 -> "商品";
            case 2 -> "用户";
            case 3 -> "店铺";
            case 4 -> "订单";
            case 5 -> "物流";
            case 6 -> "发票";
            case 7 -> "优惠券";
            case 8 -> "主图";
            default -> "其他";
        };
        vo.setBizTypeDesc(bizTypeDesc);

        return vo;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2fGB", size / (1024.0 * 1024 * 1024));
        }
    }
}
