package com.kawaiichainwallet.admin.controller;

import com.kawaiichainwallet.admin.dto.AdminRoleDto;
import com.kawaiichainwallet.admin.dto.CreateRoleRequest;
import com.kawaiichainwallet.admin.dto.UpdateRoleRequest;
import com.kawaiichainwallet.admin.service.AdminRoleService;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.common.spring.utils.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author KawaiiChain
 */
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "管理员角色增删改查接口")
public class RoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 获取所有激活的角色
     */
    @GetMapping("/active")
    @Operation(summary = "获取激活的角色列表", description = "获取所有状态为激活的角色")
    public R<List<AdminRoleDto>> getActiveRoles() {
        List<AdminRoleDto> roles = adminRoleService.getAllActiveRoles();
        return R.success(roles);
    }

    /**
     * 获取所有角色（包括未激活的）
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有角色列表", description = "获取所有角色，包括未激活的")
    public R<List<AdminRoleDto>> getAllRoles() {
        List<AdminRoleDto> roles = adminRoleService.getAllRoles();
        return R.success(roles);
    }

    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    public R<AdminRoleDto> getRoleById(
            @Parameter(description = "角色ID")
            @PathVariable Long roleId) {

        AdminRoleDto role = adminRoleService.getRoleById(roleId);
        return R.success(role);
    }

    /**
     * 创建角色
     */
    @PostMapping("/create")
    @Operation(summary = "创建角色", description = "创建新的管理员角色")
    public R<AdminRoleDto> createRole(
            @Valid @RequestBody CreateRoleRequest request) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        AdminRoleDto role = adminRoleService.createRole(request, currentAdminId);

        return R.success(role, "创建角色成功");
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    @Operation(summary = "更新角色", description = "更新角色信息和权限")
    public R<AdminRoleDto> updateRole(
            @Parameter(description = "角色ID")
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        AdminRoleDto role = adminRoleService.updateRole(roleId, request, currentAdminId);

        return R.success(role, "更新角色成功");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    @Operation(summary = "删除角色", description = "删除指定角色")
    public R<Void> deleteRole(
            @Parameter(description = "角色ID")
            @PathVariable Long roleId) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        adminRoleService.deleteRole(roleId, currentAdminId);

        return R.success("删除角色成功");
    }

    /**
     * 修改角色状态
     */
    @PutMapping("/{roleId}/status")
    @Operation(summary = "修改角色状态", description = "启用/停用角色")
    public R<Void> updateRoleStatus(
            @Parameter(description = "角色ID")
            @PathVariable Long roleId,
            @Parameter(description = "是否激活", example = "true")
            @RequestParam boolean isActive) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        adminRoleService.updateRoleStatus(roleId, isActive, currentAdminId);

        return R.success("修改状态成功");
    }
}
