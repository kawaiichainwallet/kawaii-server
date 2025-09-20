package com.kawaiichainwallet.user.service;

import com.kawaiichainwallet.user.entity.AuditLog;
import com.kawaiichainwallet.user.mapper.AuditLogMapper;
import com.kawaiichainwallet.user.converter.AuditLogConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;
    private final AuditLogConverter auditLogConverter;

    /**
     * 记录操作日志
     */
    public void logAction(String userId, String action, String resourceType, String resourceId, String metadata) {
        try {
            // 使用MapStruct converter创建审计日志
            HttpServletRequest request = getCurrentRequest();
            AuditLog auditLog = auditLogConverter.createAuditLogWithRequest(
                userId, action, resourceType, resourceId, metadata, request);

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
            // 使用MapStruct converter创建详细审计日志
            AuditLog auditLog = auditLogConverter.createDetailedAuditLog(
                userId, action, resourceType, resourceId, metadata, clientIp, userAgent, success, errorMessage);

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
            // 使用MapStruct converter创建失败审计日志
            HttpServletRequest request = getCurrentRequest();
            AuditLog auditLog = auditLogConverter.createFailedAuditLog(
                userId, action, resourceType, resourceId, metadata, errorMessage, request);

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

}