package com.kawaiichainwallet.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kawaiichainwallet.user.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

/**
 * 用户数据访问接口 - 包含认证和用户信息管理
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据登录标识查询用户 (支持用户名、邮箱、手机号)
     */
    @Select("SELECT * FROM users WHERE (username = #{identifier} OR email = #{identifier} OR phone = #{identifier}) AND status != 'deleted'")
    User findByIdentifier(@Param("identifier") String identifier);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND status != 'deleted'")
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND status != 'deleted'")
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND status != 'deleted'")
    User findByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username} AND status != 'deleted'")
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE email = #{email} AND status != 'deleted'")
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE phone = #{phone} AND status != 'deleted'")
    boolean existsByPhone(String phone);

    /**
     * 增加登录失败次数
     */
    @Update("UPDATE users SET login_attempts = login_attempts + 1, updated_at = NOW() WHERE user_id = #{userId}")
    int incrementLoginAttempts(@Param("userId") Long userId);

    /**
     * 重置登录失败次数
     */
    @Update("UPDATE users SET login_attempts = 0, updated_at = NOW() WHERE user_id = #{userId}")
    int resetLoginAttempts(@Param("userId") Long userId);

    /**
     * 锁定用户账户（时间为 UTC）
     */
    @Update("UPDATE users SET locked_until = #{lockUntil}, updated_at = NOW() WHERE user_id = #{userId}")
    int lockUser(@Param("userId") Long userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * 解锁用户账户
     */
    @Update("UPDATE users SET locked_until = NULL, login_attempts = 0, updated_at = NOW() WHERE user_id = #{userId}")
    int unlockUser(@Param("userId") Long userId);

    /**
     * 更新最后登录信息（时间为 UTC）
     */
    @Update("UPDATE users SET last_login_at = #{loginTime}, last_login_ip = #{ipAddress}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateLoginInfo(@Param("userId") Long userId,
                       @Param("loginTime") LocalDateTime loginTime,
                       @Param("ipAddress") String ipAddress);
}