package com.kawaiichainwallet.api.user.client;

import com.kawaiichainwallet.api.user.dto.IdGenerationRequest;
import com.kawaiichainwallet.api.user.dto.IdGenerationResponse;
import com.kawaiichainwallet.api.user.fallback.IdGeneratorServiceApiFallbackFactory;
import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ID生成器服务API接口定义
 * 该接口由用户服务实现，其他服务通过Feign调用
 */
@FeignClient(
    name = "kawaii-user",
    contextId = "idGeneratorServiceApi",
    path = "/user/internal/id",
    fallbackFactory = IdGeneratorServiceApiFallbackFactory.class
)
public interface IdGeneratorServiceApi {

    /**
     * 生成Segment模式ID
     */
    @PostMapping("/segment/{bizTag}")
    R<IdGenerationResponse> generateSegmentId(
        @PathVariable("bizTag") String bizTag,
        @RequestHeader("X-Internal-Token") String internalToken
    );

    /**
     * 生成Snowflake模式ID
     */
    @PostMapping("/snowflake")
    R<IdGenerationResponse> generateSnowflakeId(
        @RequestHeader("X-Internal-Token") String internalToken
    );

    /**
     * 批量生成Segment模式ID
     */
    @PostMapping("/batch/segment")
    R<IdGenerationResponse> generateBatchSegmentIds(
        @RequestBody IdGenerationRequest request,
        @RequestHeader("X-Internal-Token") String internalToken
    );

    /**
     * 获取ID生成器状态信息
     */
    @GetMapping("/status")
    R<Map<String, Object>> getGeneratorStatus(
        @RequestHeader("X-Internal-Token") String internalToken
    );

    /**
     * 根据业务标识获取当前最大ID
     */
    @GetMapping("/max-id/{bizTag}")
    R<Long> getMaxId(
        @PathVariable("bizTag") String bizTag,
        @RequestHeader("X-Internal-Token") String internalToken
    );

    /**
     * 预热ID生成器（在系统启动时调用）
     */
    @PostMapping("/warmup")
    R<String> warmupGenerator(
        @RequestHeader("X-Internal-Token") String internalToken
    );
}