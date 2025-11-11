package com.kawaiichainwallet.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员角色实体类 - 对应 admin_roles 表
 *
 * <p><b>时间字段约定</b>：所有时间字段统一使用 UTC 时区</p>
 *
 * @author KawaiiChain
 */
@Data
@TableName(value = "admin_roles", autoResultMap = true)
public class AdminRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 2961047872380545820L;

    /**
     * 角色ID (使用Leaf分布式ID生成器)
     */
    @TableId(value = "role_id", type = IdType.INPUT)
    private Long roleId;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限列表（JSONB 数组类型）
     * 例如: ["system:config", "user:manage", "audit:view"]
     */
    @TableField(value = "permissions", typeHandler = JacksonTypeHandler.class)
    private List<String> permissions;

    /**
     * 菜单权限（JSONB 数组类型）
     */
    @TableField(value = "menu_permissions", typeHandler = JacksonTypeHandler.class)
    private List<String> menuPermissions;

    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 创建时间（UTC）
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间（UTC）
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;
}
