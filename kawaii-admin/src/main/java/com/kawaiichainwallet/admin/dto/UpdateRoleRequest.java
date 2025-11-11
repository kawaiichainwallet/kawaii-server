package com.kawaiichainwallet.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新角色请求DTO
 *
 * @author KawaiiChain
 */
@Data
public class UpdateRoleRequest {

    /**
     * 角色名称
     */
    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String roleName;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述不能超过500个字符")
    private String description;

    /**
     * 权限列表
     * 例如: ["system:config", "user:manage", "audit:view"]
     */
    private List<String> permissions;

    /**
     * 菜单权限
     * 例如: ["/users", "/settings", "/audit"]
     */
    private List<String> menuPermissions;

    /**
     * 是否激活
     */
    private Boolean isActive;
}
