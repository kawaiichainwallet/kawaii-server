package com.kawaiichainwallet.user.mapper;

import com.kawaiichainwallet.user.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户
     */
    @Insert("""
        INSERT INTO users (user_id, username, email, phone, password_hash, salt,
                          status, email_verified, phone_verified, two_factor_enabled,
                          login_attempts, created_at, updated_at)
        VALUES (#{userId}, #{username}, #{email}, #{phone}, #{passwordHash}, #{salt},
                #{status}, #{emailVerified}, #{phoneVerified}, #{twoFactorEnabled},
                #{loginAttempts}, #{createdAt}, #{updatedAt})
        """)
    int insertUser(User user);

    /**
     * 根据用户ID查询用户
     */
    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    User findByUserId(String userId);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User findByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE phone = #{phone}")
    boolean existsByPhone(String phone);

    /**
     * 更新用户邮箱验证状态
     */
    @Update("UPDATE users SET email_verified = #{verified}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateEmailVerified(String userId, boolean verified);

    /**
     * 更新用户手机号验证状态
     */
    @Update("UPDATE users SET phone_verified = #{verified}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updatePhoneVerified(String userId, boolean verified);

    /**
     * 更新用户登录信息
     */
    @Update("""
        UPDATE users SET last_login_at = #{lastLoginAt}, last_login_ip = #{lastLoginIp},
                        login_attempts = 0, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId}
        """)
    int updateLoginInfo(String userId, String lastLoginAt, String lastLoginIp);

    /**
     * 增加登录失败次数
     */
    @Update("UPDATE users SET login_attempts = login_attempts + 1, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int incrementLoginAttempts(String userId);

    /**
     * 锁定用户账户
     */
    @Update("UPDATE users SET locked_until = #{lockedUntil}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int lockUser(String userId, String lockedUntil);

    /**
     * 解锁用户账户
     */
    @Update("UPDATE users SET locked_until = NULL, login_attempts = 0, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int unlockUser(String userId);

    /**
     * 更新用户密码
     */
    @Update("UPDATE users SET password_hash = #{passwordHash}, salt = #{salt}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updatePassword(String userId, String passwordHash, String salt);

    /**
     * 软删除用户
     */
    @Update("UPDATE users SET status = 'deleted', updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int softDeleteUser(String userId);
}