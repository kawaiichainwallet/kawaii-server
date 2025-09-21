package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.api.user.dto.IdGenerationRequest;
import com.kawaiichainwallet.api.user.dto.IdGenerationResponse;
import com.kawaiichainwallet.user.config.LeafConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 分布式ID生成服务
 */
@Slf4j
@Service
public class DistributedIdService {

    @Autowired
    private LeafConfiguration.SimpleSegmentIdGenerator segmentIdGenerator;

    @Autowired
    private LeafConfiguration.SimpleSnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 生成Segment模式ID
     */
    public IdGenerationResponse generateSegmentId(String bizTag) {
        try {
            Long id = segmentIdGenerator.generate(bizTag);
            log.debug("Generated segment ID: {} for bizTag: {}", id, bizTag);

            return new IdGenerationResponse(id, bizTag, "segment");
        } catch (Exception e) {
            log.error("Failed to generate segment ID for bizTag: {}", bizTag, e);
            throw new RuntimeException("Generate segment ID failed: " + e.getMessage());
        }
    }

    /**
     * 生成Snowflake模式ID
     */
    public IdGenerationResponse generateSnowflakeId() {
        try {
            Long id = snowflakeIdGenerator.generate();
            log.debug("Generated snowflake ID: {}", id);

            return new IdGenerationResponse(id, "snowflake", "snowflake");
        } catch (Exception e) {
            log.error("Failed to generate snowflake ID", e);
            throw new RuntimeException("Generate snowflake ID failed: " + e.getMessage());
        }
    }

    /**
     * 批量生成Segment模式ID
     */
    public IdGenerationResponse generateBatchSegmentIds(IdGenerationRequest request) {
        try {
            List<Long> ids = new ArrayList<>();
            String bizTag = request.getBizTag();
            int count = request.getCount() != null ? request.getCount() : 1;

            for (int i = 0; i < count; i++) {
                Long id = segmentIdGenerator.generate(bizTag);
                ids.add(id);
            }

            log.debug("Generated {} segment IDs for bizTag: {}", count, bizTag);

            return new IdGenerationResponse(ids, bizTag, "segment");
        } catch (Exception e) {
            log.error("Failed to generate batch segment IDs: {}", request, e);
            throw new RuntimeException("Generate batch segment IDs failed: " + e.getMessage());
        }
    }

    /**
     * 获取指定业务标识的当前最大ID
     */
    public Long getMaxId(String bizTag) {
        try {
            // 这里简化实现，实际可以查询数据库获取准确值
            return segmentIdGenerator.generate(bizTag) - 1;
        } catch (Exception e) {
            log.error("Failed to get max ID for bizTag: {}", bizTag, e);
            // 返回一个基于时间的估算值
            return System.currentTimeMillis() / 1000;
        }
    }

    /**
     * 预热ID生成器
     */
    public String warmupGenerator() {
        try {
            // 预热各个业务线的ID生成器
            String[] bizTags = {"user-id", "wallet-id", "transaction-id", "payment-id", "merchant-id", "notification-id"};

            for (String bizTag : bizTags) {
                try {
                    segmentIdGenerator.generate(bizTag);
                    log.info("Warmed up segment generator for bizTag: {}", bizTag);
                } catch (Exception e) {
                    log.warn("Failed to warm up segment generator for bizTag: {}", bizTag, e);
                }
            }

            // 预热Snowflake生成器
            try {
                snowflakeIdGenerator.generate();
                log.info("Warmed up snowflake generator");
            } catch (Exception e) {
                log.warn("Failed to warm up snowflake generator", e);
            }

            return "ID generators warmed up successfully";
        } catch (Exception e) {
            log.error("Failed to warm up ID generators", e);
            return "ID generators warmup failed: " + e.getMessage();
        }
    }

    /**
     * 生成降级ID（当正常生成失败时使用）
     */
    public Long generateFallbackId() {
        // 使用时间戳 + 随机数的方式生成降级ID
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return timestamp * 1000 + random;
    }

    /**
     * 检查ID生成器健康状态
     */
    public boolean isHealthy() {
        try {
            // 尝试生成一个测试ID
            Long testId = snowflakeIdGenerator.generate();
            return testId != null && testId > 0;
        } catch (Exception e) {
            log.warn("ID generator health check failed", e);
            return false;
        }
    }
}