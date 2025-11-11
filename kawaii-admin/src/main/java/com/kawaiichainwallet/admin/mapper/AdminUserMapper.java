package com.kawaiichainwallet.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kawaiichainwallet.admin.entity.AdminUser;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

/**
 * 管理员用户数据访问接口
 *
 * @author KawaiiChain
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    /**
     * 根据登录标识查询管理员 (支持用户名、邮箱)
     *
     * @param identifier 登录标识（用户名或邮箱）
     * @return 管理员用户信息
     */
    @Select("SELECT * FROM admin_users WHERE (username = #{identifier} OR email = #{identifier}) AND status != 'suspended'")
    AdminUser findByIdentifier(@Param("identifier") String identifier);

    /**
     * 根据用户名查询管理员
     *
     * @param username 用户名
     * @return 管理员用户信息
     */
    @Select("SELECT * FROM admin_users WHERE username = #{username}")
    AdminUser findByUsername(String username);

    /**
     * 根据邮箱查询管理员
     *
     * @param email 邮箱
     * @return 管理员用户信息
     */
    @Select("SELECT * FROM admin_users WHERE email = #{email}")
    AdminUser findByEmail(String email);

    /**
     * 根据手机号查询管理员
     *
     * @param phone 手机号
     * @return 管理员用户信息
     */
    @Select("SELECT * FROM admin_users WHERE phone = #{phone}")
    AdminUser findByPhone(String phone);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM admin_users WHERE username = #{username}")
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM admin_users WHERE email = #{email}")
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM admin_users WHERE phone = #{phone}")
    boolean existsByPhone(String phone);

    /**
     * 增加登录失败次数
     *
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    @Update("UPDATE admin_users SET login_attempts = login_attempts + 1, updated_at = (NOW() AT TIME ZONE 'UTC') WHERE admin_id = #{adminId}")
    int incrementLoginAttempts(@Param("adminId") Long adminId);

    /**
     * 重置登录失败次数
     *
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    @Update("UPDATE admin_users SET login_attempts = 0, updated_at = (NOW() AT TIME ZONE 'UTC') WHERE admin_id = #{adminId}")
    int resetLoginAttempts(@Param("adminId") Long adminId);

    /**
     * 锁定管理员账户（时间为 UTC）
     *
     * @param adminId 管理员ID
     * @param lockUntil 锁定截止时间
     * @return 影响的行数
     */
    @Update("UPDATE admin_users SET locked_until = #{lockUntil}, updated_at = (NOW() AT TIME ZONE 'UTC') WHERE admin_id = #{adminId}")
    int lockUser(@Param("adminId") Long adminId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * 解锁管理员账户
     *
     * @param adminId 管理员ID
     * @return 影响的行数
     */
    @Update("UPDATE admin_users SET locked_until = NULL, login_attempts = 0, updated_at = (NOW() AT TIME ZONE 'UTC') WHERE admin_id = #{adminId}")
    int unlockUser(@Param("adminId") Long adminId);

    /**
     * 更新最后登录信息（时间为 UTC）
     *
     * @param adminId 管理员ID
     * @param loginTime 登录时间
     * @param ipAddress IP地址
     * @return 影响的行数
     */
    @Update("UPDATE admin_users SET last_login_at = #{loginTime}, last_login_ip = #{ipAddress}, updated_at = (NOW() AT TIME ZONE 'UTC') WHERE admin_id = #{adminId}")
    int updateLoginInfo(@Param("adminId") Long adminId,
                       @Param("loginTime") LocalDateTime loginTime,
                       @Param("ipAddress") String ipAddress);

    /**
     * 分页查询管理员列表（支持状态筛选和关键词搜索）
     *
     * @param page 分页对象
     * @param status 状态筛选（可选）
     * @param keyword 搜索关键词（可选，搜索用户名、邮箱、真实姓名）
     * @return 分页结果
     */
    @Select({
        "<script>",
        "SELECT * FROM admin_users",
        "<where>",
        "  <if test='status != null and status != \"\"'>",
        "    AND status = #{status}",
        "  </if>",
        "  <if test='keyword != null and keyword != \"\"'>",
        "    AND (username LIKE CONCAT('%', #{keyword}, '%')",
        "         OR email LIKE CONCAT('%', #{keyword}, '%')",
        "         OR real_name LIKE CONCAT('%', #{keyword}, '%'))",
        "  </if>",
        "</where>",
        "ORDER BY created_at DESC",
        "</script>"
    })
    IPage<AdminUser> selectPageWithSearch(Page<AdminUser> page,
                                         @Param("status") String status,
                                         @Param("keyword") String keyword);
}
