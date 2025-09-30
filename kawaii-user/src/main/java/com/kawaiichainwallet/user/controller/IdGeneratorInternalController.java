package com.kawaiichainwallet.user.controller;

import com.kawaiichainwallet.api.user.client.IdGeneratorServiceApi;
import com.kawaiichainwallet.api.user.dto.IdGenerationRequest;
import com.kawaiichainwallet.api.user.dto.IdGenerationResponse;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.user.service.DistributedIdService;
import com.kawaiichainwallet.common.core.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ID生成器内部服务控制器 - 实现IdGeneratorServiceApi接口
 * 仅供内部服务调用
 */
@Slf4j
@RestController
@RequestMapping("/user/internal/id")
public class IdGeneratorInternalController implements IdGeneratorServiceApi {

    @Autowired
    private DistributedIdService distributedIdService;

    /**
     * 生成Segment模式ID
     */
    @Override
    public R<IdGenerationResponse> generateSegmentId(String bizTag) {
        log.info("内部服务生成Segment ID请求: bizTag={}", bizTag);

        try {

            IdGenerationResponse response = distributedIdService.generateSegmentId(bizTag);
            return R.success(response);
        } catch (Exception e) {
            log.error("生成Segment ID失败: bizTag={}, error={}", bizTag, e.getMessage());
            return R.error("生成Segment ID失败: " + e.getMessage());
        }
    }

    /**
     * 生成Snowflake模式ID
     */
    @Override
    public R<IdGenerationResponse> generateSnowflakeId() {
        log.info("内部服务生成Snowflake ID请求");

        try {

            IdGenerationResponse response = distributedIdService.generateSnowflakeId();
            return R.success(response);
        } catch (Exception e) {
            log.error("生成Snowflake ID失败: error={}", e.getMessage());
            return R.error("生成Snowflake ID失败: " + e.getMessage());
        }
    }

    /**
     * 批量生成Segment模式ID
     */
    @Override
    public R<IdGenerationResponse> generateBatchSegmentIds(IdGenerationRequest request) {
        log.info("内部服务批量生成Segment ID请求: {}", request);

        try {

            // 参数验证
            if (request.getBizTag() == null || request.getBizTag().trim().isEmpty()) {
                return R.error("业务标识不能为空");
            }

            if (request.getCount() == null || request.getCount() <= 0) {
                request.setCount(1);
            }

            if (request.getCount() > 1000) {
                return R.error("批量生成数量不能超过1000");
            }

            IdGenerationResponse response = distributedIdService.generateBatchSegmentIds(request);
            return R.success(response);
        } catch (Exception e) {
            log.error("批量生成Segment ID失败: request={}, error={}", request, e.getMessage());
            return R.error("批量生成Segment ID失败: " + e.getMessage());
        }
    }

    /**
     * 获取ID生成器状态信息
     */
    @Override
    public R<Map<String, Object>> getGeneratorStatus() {
        log.info("内部服务获取ID生成器状态请求");

        try {

            Map<String, Object> status = new HashMap<>();
            status.put("status", "RUNNING");
            status.put("timestamp", TimeUtil.nowUtc());
            status.put("segment_enabled", true);
            status.put("snowflake_enabled", true);
            status.put("healthy", distributedIdService.isHealthy());

            // 添加一些统计信息
            status.put("supported_biz_tags", new String[]{
                "user-id", "wallet-id", "transaction-id",
                "payment-id", "merchant-id", "notification-id"
            });

            return R.success(status);
        } catch (Exception e) {
            log.error("获取ID生成器状态失败: error={}", e.getMessage());
            return R.error("获取ID生成器状态失败: " + e.getMessage());
        }
    }

    /**
     * 根据业务标识获取当前最大ID
     */
    @Override
    public R<Long> getMaxId(String bizTag) {
        log.info("内部服务获取最大ID请求: bizTag={}", bizTag);

        try {

            Long maxId = distributedIdService.getMaxId(bizTag);
            return R.success(maxId);
        } catch (Exception e) {
            log.error("获取最大ID失败: bizTag={}, error={}", bizTag, e.getMessage());
            return R.error("获取最大ID失败: " + e.getMessage());
        }
    }

    /**
     * 预热ID生成器（在系统启动时调用）
     */
    @Override
    public R<String> warmupGenerator() {
        log.info("内部服务预热ID生成器请求");

        try {

            String result = distributedIdService.warmupGenerator();
            return R.success(result);
        } catch (Exception e) {
            log.error("预热ID生成器失败: error={}", e.getMessage());
            return R.error("预热ID生成器失败: " + e.getMessage());
        }
    }

    // ===========================================
    // 私有辅助方法
    // ===========================================


}