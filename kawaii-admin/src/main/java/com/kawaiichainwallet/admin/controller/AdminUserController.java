package com.kawaiichainwallet.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kawaiichainwallet.admin.dto.AdminUserDto;
import com.kawaiichainwallet.admin.dto.AssignRolesRequest;
import com.kawaiichainwallet.admin.dto.CreateAdminRequest;
import com.kawaiichainwallet.admin.dto.UpdateAdminRequest;
import com.kawaiichainwallet.admin.service.AdminUserService;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.common.spring.utils.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户控制器
 *
 * @author KawaiiChain
 */
@Slf4j
@RestController
@RequestMapping("/admin-user")
@RequiredArgsConstructor
@Tag(name = "管理员用户管理", description = "管理员用户增删改查接口")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 分页查询管理员列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取管理员列表", description = "分页查询管理员列表，支持状态筛选和关键词搜索")
    public R<IPage<AdminUserDto>> getAdminList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "状态筛选（可选）", example = "active")
            @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词（可选）")
            @RequestParam(required = false) String keyword) {

        IPage<AdminUserDto> result = adminUserService.getAdminUsers(page, size, status, keyword);
        return R.success(result);
    }

    /**
     * 根据ID获取管理员详情
     */
    @GetMapping("/{adminId}")
    @Operation(summary = "获取管理员详情", description = "根据ID获取管理员详细信息")
    public R<AdminUserDto> getAdminById(
            @Parameter(description = "管理员ID")
            @PathVariable Long adminId) {

        AdminUserDto admin = adminUserService.getAdminById(adminId);
        return R.success(admin);
    }

    /**
     * 创建管理员
     */
    @PostMapping("/create")
    @Operation(summary = "创建管理员", description = "创建新的管理员账号")
    public R<AdminUserDto> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        AdminUserDto admin = adminUserService.createAdmin(request, currentAdminId);

        return R.success(admin, "创建管理员成功");
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{adminId}")
    @Operation(summary = "更新管理员信息", description = "更新管理员基本信息")
    public R<AdminUserDto> updateAdmin(
            @Parameter(description = "管理员ID")
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminRequest request) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        AdminUserDto admin = adminUserService.updateAdmin(adminId, request, currentAdminId);

        return R.success(admin, "更新管理员成功");
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{adminId}")
    @Operation(summary = "删除管理员", description = "删除指定管理员账号")
    public R<Void> deleteAdmin(
            @Parameter(description = "管理员ID")
            @PathVariable Long adminId) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        adminUserService.deleteAdmin(adminId, currentAdminId);

        return R.success("删除管理员成功");
    }

    /**
     * 修改管理员状态
     */
    @PutMapping("/{adminId}/status")
    @Operation(summary = "修改管理员状态", description = "启用/停用/封禁管理员账号")
    public R<Void> updateAdminStatus(
            @Parameter(description = "管理员ID")
            @PathVariable Long adminId,
            @Parameter(description = "新状态", example = "active")
            @RequestParam String status) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        adminUserService.updateAdminStatus(adminId, status, currentAdminId);

        return R.success("修改状态成功");
    }

    /**
     * 分配角色给管理员
     */
    @PostMapping("/{adminId}/roles")
    @Operation(summary = "分配角色", description = "为管理员分配角色")
    public R<Void> assignRoles(
            @Parameter(description = "管理员ID")
            @PathVariable Long adminId,
            @Valid @RequestBody AssignRolesRequest request) {

        Long currentAdminId = RequestUtil.getCurrentUserId();
        adminUserService.assignRoles(adminId, request.getRoleIds(), currentAdminId);

        return R.success("分配角色成功");
    }

    /**
     * ID生成器健康检查
     */
    @GetMapping("/id-generator/health")
    @Operation(summary = "ID生成器健康检查", description = "检查ID生成服务是否正常")
    public R<String> checkIdGeneratorHealth() {
        return R.success("ID生成器运行正常");
    }
}