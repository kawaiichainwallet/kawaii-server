package com.kawaiichainwallet.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kawaiichainwallet.admin.entity.AdminRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理员角色数据访问接口
 *
 * @author KawaiiChain
 */
@Mapper
public interface AdminRoleMapper extends BaseMapper<AdminRole> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    @Select("SELECT * FROM admin_roles WHERE role_code = #{roleCode}")
    AdminRole findByRoleCode(String roleCode);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM admin_roles WHERE role_code = #{roleCode}")
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色名称是否存在
     *
     * @param roleName 角色名称
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM admin_roles WHERE role_name = #{roleName}")
    boolean existsByRoleName(String roleName);

    /**
     * 查询所有激活的角色
     *
     * @return 激活的角色列表
     */
    @Select("SELECT * FROM admin_roles WHERE is_active = true ORDER BY created_at ASC")
    List<AdminRole> findAllActive();

    /**
     * 根据角色ID列表批量查询角色
     *
     * @param roleIds 角色ID列表
     * @return 角色列表
     */
    @Select({
        "<script>",
        "SELECT * FROM admin_roles",
        "WHERE role_id IN",
        "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>",
        "  #{roleId}",
        "</foreach>",
        "</script>"
    })
    List<AdminRole> findByIds(@Param("roleIds") List<Long> roleIds);
}
