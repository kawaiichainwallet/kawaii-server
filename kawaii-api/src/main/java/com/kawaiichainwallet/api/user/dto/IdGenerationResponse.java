package com.kawaiichainwallet.api.user.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ID生成响应DTO
 */
@Data
public class IdGenerationResponse {

    /**
     * 生成的单个ID
     */
    private Long id;

    /**
     * 批量生成的ID列表
     */
    private List<Long> ids;

    /**
     * 业务标识
     */
    private String bizTag;

    /**
     * ID生成类型 (segment, snowflake)
     */
    private String type;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 生成数量
     */
    private Integer count;

    /**
     * 生成器状态
     */
    private String status;

    public IdGenerationResponse() {
        this.generatedAt = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public IdGenerationResponse(Long id, String bizTag, String type) {
        this();
        this.id = id;
        this.bizTag = bizTag;
        this.type = type;
        this.count = 1;
    }

    public IdGenerationResponse(List<Long> ids, String bizTag, String type) {
        this();
        this.ids = ids;
        this.bizTag = bizTag;
        this.type = type;
        this.count = ids != null ? ids.size() : 0;
    }
}