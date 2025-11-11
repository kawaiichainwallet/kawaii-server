package com.kawaiichainwallet.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员角色关联实体类 - 对应 admin_user_roles 表
 *
 * <p><b>时间字段约定</b>：所有时间字段统一使用 UTC 时区</p>
 *
 * @author KawaiiChain
 */
@Data
@TableName("admin_user_roles")
public class AdminUserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 7354910381736286425L;

    /**
     * 关联ID (自增主键)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 分配时间（UTC）
     */
    @TableField("assigned_at")
    private LocalDateTime assignedAt;

    /**
     * 分配人ID
     */
    @TableField("assigned_by")
    private Long assignedBy;

    /**
     * 角色过期时间（UTC，可选）
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;
}
