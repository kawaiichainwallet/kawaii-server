package com.kawaiichainwallet.common.core.entity;

import com.kawaiichainwallet.common.core.utils.TimeUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * <p><b>时间字段约定</b>：
 * 所有 LocalDateTime 类型的时间字段统一使用 UTC 时区存储，
 * 数据库中使用 TIMESTAMP 类型（不带时区）。
 * 应用层负责时区转换，确保写入数据库的时间都是 UTC。
 * </p>
 *
 * <p><b>使用示例</b>：
 * <pre>
 * // ✅ 正确：使用 TimeUtil 工具类获取 UTC 时间
 * entity.setCreateTime();  // 内部调用 TimeUtil.nowUtc()
 *
 * // ✅ 正确：手动设置 UTC 时间
 * entity.setCreatedAt(TimeUtil.nowUtc());
 *
 * // ❌ 错误：使用系统默认时区
 * entity.setCreatedAt(LocalDateTime.now());  // 不要这样做！
 * </pre>
 * </p>
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6555385177473586559L;

    /**
     * 创建时间（UTC）
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间（UTC）
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
        LocalDateTime now = TimeUtil.nowUtc();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 设置更新时间（UTC）
     */
    public void setUpdateTime() {
        this.updatedAt = TimeUtil.nowUtc();
    }
}