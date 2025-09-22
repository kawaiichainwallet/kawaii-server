package com.kawaiichainwallet.user.component;

import com.kawaiichainwallet.user.service.DistributedIdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * ID生成器自动预热组件
 * 在应用启动时自动触发分布式ID生成器预热
 */
@Slf4j
@Component
public class IdGeneratorWarmupComponent {

    @Autowired
    private DistributedIdService distributedIdService;

    /**
     * 应用启动后自动预热ID生成器
     */
    @PostConstruct
    public void initWarmup() {
        log.info("开始执行分布式ID生成器自动预热...");

        try {
            // 异步执行预热，避免阻塞应用启动
            warmupIdGeneratorsAsync();
        } catch (Exception e) {
            log.error("分布式ID生成器预热初始化失败，但不影响应用启动", e);
        }
    }

    /**
     * 异步预热ID生成器
     */
    @Async
    public void warmupIdGeneratorsAsync() {
        try {
            log.info("正在预热分布式ID生成器...");

            // 调用预热方法
            String result = distributedIdService.warmupGenerator();

            log.info("分布式ID生成器预热完成: {}", result);

            // 验证预热效果
            validateWarmupResult();

        } catch (Exception e) {
            log.error("分布式ID生成器预热过程中发生异常", e);
        }
    }

    /**
     * 验证预热效果
     */
    private void validateWarmupResult() {
        try {
            // 检查ID生成器健康状态
            boolean healthy = distributedIdService.isHealthy();

            if (healthy) {
                log.info("分布式ID生成器预热验证通过，系统运行正常");
            } else {
                log.warn("分布式ID生成器预热验证失败，请检查系统状态");
            }

        } catch (Exception e) {
            log.warn("分布式ID生成器预热验证过程中发生异常", e);
        }
    }
}