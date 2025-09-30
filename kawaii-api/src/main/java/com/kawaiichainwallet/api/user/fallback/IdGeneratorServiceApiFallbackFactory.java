package com.kawaiichainwallet.api.user.fallback;

import com.kawaiichainwallet.api.user.client.IdGeneratorServiceApi;
import com.kawaiichainwallet.api.user.dto.IdGenerationRequest;
import com.kawaiichainwallet.api.user.dto.IdGenerationResponse;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成器服务API降级处理工厂
 */
@Slf4j
@Component
public class IdGeneratorServiceApiFallbackFactory implements FallbackFactory<IdGeneratorServiceApi> {

    @Override
    public IdGeneratorServiceApi create(Throwable cause) {
        return new IdGeneratorServiceApi() {

            @Override
            public R<IdGenerationResponse> generateSegmentId(String bizTag) {
                log.error("生成Segment ID失败: bizTag={}", bizTag, cause);
                // 降级策略：生成基于时间戳的临时ID
                IdGenerationResponse response = new IdGenerationResponse();
                response.setId(generateFallbackId());
                response.setBizTag(bizTag);
                response.setType("segment");
                response.setGeneratedAt(LocalDateTime.now());
                response.setStatus("FALLBACK");
                return R.ok(response);
            }

            @Override
            public R<IdGenerationResponse> generateSnowflakeId() {
                log.error("生成Snowflake ID失败", cause);
                IdGenerationResponse response = new IdGenerationResponse();
                response.setId(generateFallbackId());
                response.setType("snowflake");
                response.setGeneratedAt(LocalDateTime.now());
                response.setStatus("FALLBACK");
                return R.ok(response);
            }

            @Override
            public R<IdGenerationResponse> generateBatchSegmentIds(IdGenerationRequest request) {
                log.error("批量生成Segment ID失败: request={}", request, cause);
                IdGenerationResponse response = new IdGenerationResponse();
                List<Long> fallbackIds = new ArrayList<>();
                int count = request.getCount() != null ? request.getCount() : 1;
                for (int i = 0; i < count; i++) {
                    fallbackIds.add(generateFallbackId());
                }
                response.setIds(fallbackIds);
                response.setBizTag(request.getBizTag());
                response.setType("segment");
                response.setGeneratedAt(LocalDateTime.now());
                response.setStatus("FALLBACK");
                response.setCount(count);
                return R.ok(response);
            }

            @Override
            public R<Map<String, Object>> getGeneratorStatus() {
                log.error("获取ID生成器状态失败", cause);
                Map<String, Object> status = new HashMap<>();
                status.put("status", "DEGRADED");
                status.put("message", "Service temporarily unavailable");
                status.put("timestamp", LocalDateTime.now());
                status.put("fallback_mode", true);
                return R.ok(status);
            }

            @Override
            public R<Long> getMaxId(String bizTag) {
                log.error("获取最大ID失败: bizTag={}", bizTag, cause);
                // 返回一个基于当前时间的大概值
                Long fallbackMaxId = System.currentTimeMillis() / 1000;
                return R.ok(fallbackMaxId);
            }

            @Override
            public R<String> warmupGenerator() {
                log.error("预热ID生成器失败", cause);
                return R.error(ApiCode.SERVICE_UNAVAILABLE, "ID生成器预热失败，使用降级模式");
            }

            /**
             * 降级ID生成策略：时间戳 + 随机数
             * 格式：时间戳(毫秒) * 1000 + 3位随机数
             * 确保生成的ID在合理范围内且相对唯一
             */
            private Long generateFallbackId() {
                long timestamp = System.currentTimeMillis();
                int random = ThreadLocalRandom.current().nextInt(100, 1000);
                return timestamp * 1000 + random;
            }
        };
    }
}