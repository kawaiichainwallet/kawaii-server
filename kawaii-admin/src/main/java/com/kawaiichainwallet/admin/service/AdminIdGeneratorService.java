package com.kawaiichainwallet.admin.service;

import com.kawaiichainwallet.api.user.client.IdGeneratorServiceApi;
import com.kawaiichainwallet.api.user.dto.IdGenerationRequest;
import com.kawaiichainwallet.api.user.dto.IdGenerationResponse;
import com.kawaiichainwallet.common.core.exception.IdGenerationException;
import com.kawaiichainwallet.common.core.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Admin服务ID生成器服务
 * 通过Feign调用user服务的分布式ID生成器
 *
 * @author kawaii-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminIdGeneratorService {

    private final IdGeneratorServiceApi idGeneratorServiceApi;

    // 业务标识常量 - 直接使用user服务中定义的业务标识
    private static final String ADMIN_USER_BIZ_TAG = "user-id";
    private static final String AUDIT_LOG_BIZ_TAG = "notification-id";
    private static final String CONFIG_BIZ_TAG = "user-id";

    /**
     * 生成管理员用户ID
     */
    public Long generateAdminUserId() {
        try {
            R<IdGenerationResponse> result = idGeneratorServiceApi.generateSegmentId(ADMIN_USER_BIZ_TAG);
            if (result.isSuccess() && result.getData() != null) {
                Long id = result.getData().getId();
                log.debug("Generated admin user ID: {}", id);
                return id;
            } else {
                log.error("Failed to generate admin user ID: {}", result.getMsg());
                throw new IdGenerationException("Generate ID failed: " + result.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to generate admin user ID", e);
            throw new IdGenerationException("Generate ID failed", e);
        }
    }

    /**
     * 生成审计日志ID
     */
    public Long generateAuditLogId() {
        try {
            R<IdGenerationResponse> result = idGeneratorServiceApi.generateSegmentId(AUDIT_LOG_BIZ_TAG);
            if (result.isSuccess() && result.getData() != null) {
                Long id = result.getData().getId();
                log.debug("Generated audit log ID: {}", id);
                return id;
            } else {
                log.error("Failed to generate audit log ID: {}", result.getMsg());
                throw new IdGenerationException("Generate ID failed: " + result.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to generate audit log ID", e);
            throw new IdGenerationException("Generate ID failed", e);
        }
    }

    /**
     * 生成系统配置ID
     */
    public Long generateConfigId() {
        try {
            R<IdGenerationResponse> result = idGeneratorServiceApi.generateSegmentId(CONFIG_BIZ_TAG);
            if (result.isSuccess() && result.getData() != null) {
                Long id = result.getData().getId();
                log.debug("Generated config ID: {}", id);
                return id;
            } else {
                log.error("Failed to generate config ID: {}", result.getMsg());
                throw new IdGenerationException("Generate ID failed: " + result.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to generate config ID", e);
            throw new IdGenerationException("Generate ID failed", e);
        }
    }

    /**
     * 生成角色ID
     */
    public Long generateRoleId() {
        return generateAdminUserId(); // 角色ID使用user-id业务标识
    }

    /**
     * 批量生成ID
     */
    public IdGenerationResponse generateBatchIds(String bizTag, int count) {
        try {
            IdGenerationRequest request = new IdGenerationRequest();
            request.setBizTag(bizTag);
            request.setCount(count);

            R<IdGenerationResponse> result = idGeneratorServiceApi.generateBatchSegmentIds(request);
            if (result.isSuccess() && result.getData() != null) {
                log.debug("Generated {} IDs for bizTag: {}", count, bizTag);
                return result.getData();
            } else {
                log.error("Failed to generate batch IDs: {}", result.getMsg());
                throw new IdGenerationException("Generate batch IDs failed: " + result.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to generate batch IDs for bizTag: {}, count: {}", bizTag, count, e);
            throw new IdGenerationException("Generate batch IDs failed", e);
        }
    }

    /**
     * 检查ID生成器状态
     */
    public boolean isGeneratorHealthy() {
        try {
            R<?> result = idGeneratorServiceApi.getGeneratorStatus();
            return result.isSuccess();
        } catch (Exception e) {
            log.warn("Failed to check ID generator health", e);
            return false;
        }
    }
}