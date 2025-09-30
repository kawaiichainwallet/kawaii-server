package com.kawaiichainwallet.common.core.entity;

import com.kawaiichainwallet.common.core.utils.TimeUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6555385177473586559L;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 更新人ID
     */
    private String updatedBy;

    /**
     * 设置创建时间（UTC）
     */
    public void setCreateTime() {
        this.createdAt = TimeUtil.nowUtc();
        this.updatedAt = TimeUtil.nowUtc();
    }

    /**
     * 设置更新时间（UTC）
     */
    public void setUpdateTime() {
        this.updatedAt = TimeUtil.nowUtc();
    }
}