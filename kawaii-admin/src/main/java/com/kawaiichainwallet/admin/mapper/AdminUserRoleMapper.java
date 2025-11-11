package com.kawaiichainwallet.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kawaiichainwallet.admin.entity.AdminRole;
import com.kawaiichainwallet.admin.entity.AdminUserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理员角色关联数据访问接口
 *
 * @author KawaiiChain
 */
@Mapper
public interface AdminUserRoleMapper extends BaseMapper<AdminUserRole> {

    /**
     * 根据管理员ID查询其所有角色
     *
     * @param adminId 管理员ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM admin_roles r " +
            "INNER JOIN admin_user_roles ur ON r.role_id = ur.role_id " +
            "WHERE ur.admin_id = #{adminId} AND r.is_active = true " +
            "AND (ur.expires_at IS NULL OR ur.expires_at > (NOW() AT TIME ZONE 'UTC'))")
    List<AdminRole> findRolesByAdminId(@Param("adminId") Long adminId);

    /**
     * 根据管理员ID查询角色ID列表
     *
     * @param adminId 管理员ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM admin_user_roles WHERE admin_id = #{adminId}")
    List<Long> findRoleIdsByAdminId(@Param("adminId") Long adminId);

    /**
     * 根据角色ID查询拥有该角色的管理员ID列表
     *
     * @param roleId 角色ID
     * @return 管理员ID列表
     */
    @Select("SELECT admin_id FROM admin_user_roles WHERE role_id = #{roleId}")
    List<Long> findAdminIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查管理员是否拥有指定角色
     *
     * @param adminId 管理员ID
     * @param roleId 角色ID
     * @return 是否拥有
     */
    @Select("SELECT COUNT(1) > 0 FROM admin_user_roles WHERE admin_id = #{adminId} AND role_id = #{roleId}")
    boolean existsByAdminIdAndRoleId(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    /**
     * 删除管理员的所有角色
     *
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM admin_user_roles WHERE admin_id = #{adminId}")
    int deleteByAdminId(@Param("adminId") Long adminId);

    /**
     * 删除管理员的指定角色
     *
     * @param adminId 管理员ID
     * @param roleId 角色ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM admin_user_roles WHERE admin_id = #{adminId} AND role_id = #{roleId}")
    int deleteByAdminIdAndRoleId(@Param("adminId") Long adminId, @Param("roleId") Long roleId);

    /**
     * 批量分配角色给管理员
     *
     * @param adminId 管理员ID
     * @param roleIds 角色ID列表
     * @param assignedBy 分配人ID
     * @return 影响的行数
     */
    @Insert({
        "<script>",
        "INSERT INTO admin_user_roles (admin_id, role_id, assigned_at, assigned_by)",
        "VALUES",
        "<foreach collection='roleIds' item='roleId' separator=','>",
        "  (#{adminId}, #{roleId}, (NOW() AT TIME ZONE 'UTC'), #{assignedBy})",
        "</foreach>",
        "</script>"
    })
    int batchInsert(@Param("adminId") Long adminId,
                   @Param("roleIds") List<Long> roleIds,
                   @Param("assignedBy") Long assignedBy);
}
