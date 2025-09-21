package com.kawaiichainwallet.api.user.dto;

import lombok.Data;

/**
 * ID生成请求DTO
 */
@Data
public class IdGenerationRequest {

    /**
     * 业务标识 (user-id, wallet-id, transaction-id等)
     */
    private String bizTag;

    /**
     * 批量生成数量 (默认1)
     */
    private Integer count = 1;

    /**
     * ID生成类型 (segment, snowflake)
     */
    private String type;

    public IdGenerationRequest() {
    }

    public IdGenerationRequest(String bizTag) {
        this.bizTag = bizTag;
        this.count = 1;
        this.type = "segment";
    }

    public IdGenerationRequest(String bizTag, Integer count) {
        this.bizTag = bizTag;
        this.count = count;
        this.type = "segment";
    }

    public IdGenerationRequest(String bizTag, Integer count, String type) {
        this.bizTag = bizTag;
        this.count = count;
        this.type = type;
    }
}