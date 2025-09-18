package com.kawaiichainwallet.user.mapper;

import com.kawaiichainwallet.user.entity.AuditLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审计日志数据访问接口
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     */
    @Insert("""
        INSERT INTO audit_logs (log_id, user_id, action, resource_type, resource_id,
                               ip_address, user_agent, request_path, request_method,
                               old_values, new_values, metadata, success, error_message,
                               created_at)
        VALUES (#{logId}, #{userId}, #{action}, #{resourceType}, #{resourceId},
                #{ipAddress}, #{userAgent}, #{requestPath}, #{requestMethod},
                #{oldValues}, #{newValues}, #{metadata}, #{success}, #{errorMessage},
                #{createdAt})
        """)
    int insertAuditLog(AuditLog auditLog);

    /**
     * 根据用户ID查询审计日志
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<AuditLog> findByUserId(String userId, int limit, int offset);

    /**
     * 根据操作动作查询审计日志
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE action = #{action}
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<AuditLog> findByAction(String action, int limit, int offset);

    /**
     * 根据资源类型查询审计日志
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE resource_type = #{resourceType}
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<AuditLog> findByResourceType(String resourceType, int limit, int offset);

    /**
     * 查询用户的最近登录记录
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE user_id = #{userId} AND action = 'LOGIN'
        ORDER BY created_at DESC
        LIMIT #{limit}
        """)
    List<AuditLog> findUserLoginHistory(String userId, int limit);
}