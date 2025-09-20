package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.user.entity.AuditLog;
import com.kawaiichainwallet.user.mapper.AuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 审计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    /**
     * 记录操作日志
     */
    public void logAction(String userId, String action, String resourceType, String resourceId, String metadata) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setLogId(UUID.randomUUID().toString());
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setMetadata(metadata);
            auditLog.setSuccess(true);
            auditLog.setCreatedAt(LocalDateTime.now());

            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestPath(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            auditLogMapper.insert(auditLog);
            log.debug("记录审计日志: userId={}, action={}, resourceType={}", userId, action, resourceType);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 记录操作日志（支持自定义IP和UserAgent）
     */
    public void logAction(String userId, String action, String resourceType, String resourceId,
                         String metadata, String clientIp, String userAgent, boolean success, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setLogId(UUID.randomUUID().toString());
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setMetadata(metadata);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setIpAddress(clientIp);
            auditLog.setUserAgent(userAgent);
            auditLog.setCreatedAt(LocalDateTime.now());

            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setRequestPath(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            auditLogMapper.insert(auditLog);
            log.debug("记录审计日志: userId={}, action={}, success={}", userId, action, success);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    /**
     * 记录失败操作
     */
    public void logFailedAction(String userId, String action, String resourceType, String resourceId,
                               String errorMessage, String metadata) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setLogId(UUID.randomUUID().toString());
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setMetadata(metadata);
            auditLog.setSuccess(false);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setCreatedAt(LocalDateTime.now());

            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestPath(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            auditLogMapper.insert(auditLog);
            log.debug("记录失败审计日志: userId={}, action={}, error={}", userId, action, errorMessage);
        } catch (Exception e) {
            log.error("记录失败审计日志失败", e);
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
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