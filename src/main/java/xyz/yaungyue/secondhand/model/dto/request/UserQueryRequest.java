package xyz.yaungyue.secondhand.model.dto.request;

import lombok.Data;

/**
 * 用户查询请求
 */
@Data
public class UserQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 20;

    /**
     * 搜索关键字（用户名、昵称、手机号）
     */
    private String keyword;

    /**
     * 用户状态（0-禁用，1-正常）
     */
    private Integer status;
}
