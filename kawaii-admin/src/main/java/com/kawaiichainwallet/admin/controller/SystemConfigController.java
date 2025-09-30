package com.kawaiichainwallet.admin.controller;

import com.kawaiichainwallet.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置管理控制器
 *
 * @author kawaii-server
 */
@Tag(name = "系统配置管理", description = "系统配置的增删改查操作")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Slf4j
public class SystemConfigController {

    @Operation(summary = "获取系统配置列表")
    @GetMapping("/list")
    public R<?> getConfigList() {
        log.info("获取系统配置列表");
        return R.success("系统配置列表功能待实现");
    }

    @Operation(summary = "根据配置组获取配置")
    @GetMapping("/group/{group}")
    public R<?> getConfigByGroup(@PathVariable String group) {
        log.info("获取配置组 {} 的配置", group);
        return R.success("根据配置组获取配置功能待实现");
    }

    @Operation(summary = "更新系统配置")
    @PutMapping("/{configId}")
    public R<?> updateConfig(@PathVariable Long configId, @RequestBody Object configData) {
        log.info("更新配置ID {} 的配置", configId);
        return R.success("更新系统配置功能待实现");
    }
}