package com.kawaiichainwallet.user.converter;

import com.kawaiichainwallet.user.entity.AuditLog;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 审计日志对象转换器
 * 用于简化AuditLog对象的创建
 */
@Component
public class AuditLogConverter {

    /**
     * 创建基础审计日志对象
     */
    public AuditLog createAuditLog(String userId, String action, String resourceType, String resourceId, String metadata) {
        AuditLog auditLog = new AuditLog();
        auditLog.setLogId(UUID.randomUUID().toString());
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setMetadata(metadata);
        auditLog.setSuccess(true);
        auditLog.setCreatedAt(LocalDateTime.now());
        return auditLog;
    }

    /**
     * 创建包含HTTP请求信息的审计日志对象
     */
    public AuditLog createAuditLogWithRequest(String userId, String action, String resourceType,
                                              String resourceId, String metadata, HttpServletRequest request) {
        AuditLog auditLog = createAuditLog(userId, action, resourceType, resourceId, metadata);

        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setRequestPath(request.getRequestURI());
            auditLog.setRequestMethod(request.getMethod());
        }

        return auditLog;
    }

    /**
     * 创建失败的审计日志
     */
    public AuditLog createFailedAuditLog(String userId, String action, String resourceType,
                                         String resourceId, String metadata, String errorMessage,
                                         HttpServletRequest request) {
        AuditLog auditLog = createAuditLogWithRequest(userId, action, resourceType, resourceId, metadata, request);
        auditLog.setSuccess(false);
        auditLog.setErrorMessage(errorMessage);
        return auditLog;
    }

    /**
     * 创建详细的审计日志
     */
    public AuditLog createDetailedAuditLog(String userId, String action, String resourceType,
                                           String resourceId, String metadata, String clientIp,
                                           String userAgent, boolean success, String errorMessage) {
        AuditLog auditLog = createAuditLog(userId, action, resourceType, resourceId, metadata);
        auditLog.setIpAddress(clientIp);
        auditLog.setUserAgent(userAgent);
        auditLog.setSuccess(success);
        auditLog.setErrorMessage(errorMessage);
        return auditLog;
    }

    /**
     * 获取客户端IP地址
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}