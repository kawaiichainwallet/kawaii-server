package com.kawaiichainwallet.user.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体类 - 对应 audit_logs 表
 */
@Data
public class AuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = -2494021046080924097L;

    /**
     * 日志ID
     */
    private String logId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 操作动作
     */
    private String action;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 修改前的值
     */
    private String oldValues;

    /**
     * 修改后的值
     */
    private String newValues;

    /**
     * 元数据
     */
    private String metadata;

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}