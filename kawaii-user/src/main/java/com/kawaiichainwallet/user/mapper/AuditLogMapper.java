package com.kawaiichainwallet.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kawaiichainwallet.user.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审计日志数据访问接口 - 认证服务专用
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 根据用户ID查询审计日志
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<AuditLog> findByUserId(@Param("userId") String userId,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    /**
     * 根据操作动作查询审计日志
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE action = #{action}
        ORDER BY created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<AuditLog> findByAction(@Param("action") String action,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    /**
     * 查询用户的最近登录记录
     */
    @Select("""
        SELECT * FROM audit_logs
        WHERE user_id = #{userId} AND action IN ('LOGIN_SUCCESS', 'LOGIN_FAILED')
        ORDER BY created_at DESC
        LIMIT #{limit}
        """)
    List<AuditLog> findUserLoginHistory(@Param("userId") String userId,
                                       @Param("limit") int limit);
}