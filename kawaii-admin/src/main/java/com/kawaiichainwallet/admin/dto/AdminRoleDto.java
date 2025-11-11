package com.kawaiichainwallet.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员角色详细信息DTO
 *
 * <p><b>时间字段约定</b>：所有时间字段统一使用 UTC 时区</p>
 *
 * @author KawaiiChain
 */
@Data
public class AdminRoleDto {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 菜单权限
     */
    private List<String> menuPermissions;

    /**
     * 是否激活
     */
    private Boolean isActive;

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
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;
}
