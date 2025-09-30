package com.kawaiichainwallet.admin.controller;

import com.kawaiichainwallet.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 运营审计日志控制器
 *
 * @author kawaii-server
 */
@Tag(name = "运营审计日志", description = "运营级别的审计日志查询和管理")
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    @Operation(summary = "获取审计日志列表")
    @GetMapping("/logs")
    public R<?> getAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType
    ) {
        log.info("获取审计日志列表，页码：{}，大小：{}，操作：{}，资源类型：{}", page, size, action, resourceType);
        return R.success("审计日志列表功能待实现");
    }

    @Operation(summary = "根据用户ID获取审计日志")
    @GetMapping("/user/{userId}")
    public R<?> getAuditLogsByUser(@PathVariable Long userId) {
        log.info("获取用户 {} 的审计日志", userId);
        return R.success("根据用户获取审计日志功能待实现");
    }

    @Operation(summary = "记录运营审计日志")
    @PostMapping("/record")
    public R<?> recordAuditLog(@RequestBody Object auditData) {
        log.info("记录运营审计日志");
        return R.success("记录审计日志功能待实现");
    }
}