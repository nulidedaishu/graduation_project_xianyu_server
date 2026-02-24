package xyz.yaungyue.secondhand.model.dto.request;

/**
 * 商品查询条件
 */
public record ProductQueryRequest(
    Integer status, // 商品状态
    Long categoryId, // 分类ID
    String keyword, // 关键词搜索
    Integer page, // 页码
    Integer size // 每页大小
) {
    public ProductQueryRequest {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
    }
}