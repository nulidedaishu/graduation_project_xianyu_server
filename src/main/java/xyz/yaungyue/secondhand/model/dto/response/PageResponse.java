package xyz.yaungyue.secondhand.model.dto.response;

import java.util.List;

/**
 * 分页响应
 */
public record PageResponse<T>(
    List<T> records,
    Long total,
    Integer current,
    Integer size,
    Integer pages
) {
    public static <T> PageResponse<T> of(List<T> records, Long total, Integer current, Integer size) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageResponse<>(records, total, current, size, pages);
    }
}