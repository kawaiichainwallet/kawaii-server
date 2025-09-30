package com.kawaiichainwallet.admin.controller;

import com.kawaiichainwallet.admin.service.AdminIdGeneratorService;
import com.kawaiichainwallet.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户控制器
 *
 * @author kawaii-server
 */
@Tag(name = "管理员用户管理", description = "管理员账户的增删改查和权限管理")
@RestController
@RequestMapping("/admin-user")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminIdGeneratorService adminIdGeneratorService;

    @Operation(summary = "获取管理员用户列表")
    @GetMapping("/list")
    public R<?> getAdminUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("获取管理员用户列表，页码：{}，大小：{}", page, size);
        return R.success("管理员用户列表功能待实现");
    }

    @Operation(summary = "创建管理员用户")
    @PostMapping("/create")
    public R<?> createAdminUser(@RequestBody Object adminUserData) {
        log.info("创建管理员用户");

        // 演示如何生成分布式ID
        Long adminId = adminIdGeneratorService.generateAdminUserId();
        log.info("为新管理员分配ID: {}", adminId);

        return R.success("创建管理员用户功能待实现，分配的ID: " + adminId);
    }

    @Operation(summary = "更新管理员用户")
    @PutMapping("/{adminId}")
    public R<?> updateAdminUser(@PathVariable Long adminId, @RequestBody Object adminUserData) {
        log.info("更新管理员用户：{}", adminId);
        return R.success("更新管理员用户功能待实现");
    }

    @Operation(summary = "禁用/启用管理员用户")
    @PutMapping("/{adminId}/status")
    public R<?> toggleAdminUserStatus(@PathVariable Long adminId, @RequestParam String status) {
        log.info("切换管理员用户 {} 状态为：{}", adminId, status);
        return R.success("切换管理员用户状态功能待实现");
    }

    @Operation(summary = "分配角色给管理员")
    @PostMapping("/{adminId}/roles")
    public R<?> assignRoles(@PathVariable Long adminId, @RequestBody Object roleData) {
        log.info("为管理员 {} 分配角色", adminId);
        return R.success("分配角色功能待实现");
    }

    @Operation(summary = "检查ID生成器健康状态")
    @GetMapping("/id-generator/health")
    public R<?> checkIdGeneratorHealth() {
        boolean isHealthy = adminIdGeneratorService.isGeneratorHealthy();
        return isHealthy ?
            R.success("ID生成器状态正常") :
            R.error("ID生成器状态异常");
    }
}