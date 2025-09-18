package com.kawaiichainwallet.user.mapper;

import com.kawaiichainwallet.user.entity.UserProfile;
import org.apache.ibatis.annotations.*;

/**
 * 用户资料数据访问接口
 */
@Mapper
public interface UserProfileMapper {

    /**
     * 插入用户资料
     */
    @Insert("""
        INSERT INTO user_profiles (profile_id, user_id, first_name, last_name, display_name,
                                  avatar_url, birth_date, gender, country, state_province,
                                  city, postal_code, address_line1, address_line2,
                                  language, timezone, currency, notifications_enabled,
                                  created_at, updated_at)
        VALUES (#{profileId}, #{userId}, #{firstName}, #{lastName}, #{displayName},
                #{avatarUrl}, #{birthDate}, #{gender}, #{country}, #{stateProvince},
                #{city}, #{postalCode}, #{addressLine1}, #{addressLine2},
                #{language}, #{timezone}, #{currency}, #{notificationsEnabled},
                #{createdAt}, #{updatedAt})
        """)
    int insertUserProfile(UserProfile userProfile);

    /**
     * 根据用户ID查询用户资料
     */
    @Select("SELECT * FROM user_profiles WHERE user_id = #{userId}")
    UserProfile findByUserId(String userId);

    /**
     * 更新用户资料
     */
    @Update("""
        UPDATE user_profiles SET
            first_name = #{firstName}, last_name = #{lastName}, display_name = #{displayName},
            avatar_url = #{avatarUrl}, birth_date = #{birthDate}, gender = #{gender},
            country = #{country}, state_province = #{stateProvince}, city = #{city},
            postal_code = #{postalCode}, address_line1 = #{addressLine1}, address_line2 = #{addressLine2},
            language = #{language}, timezone = #{timezone}, currency = #{currency},
            notifications_enabled = #{notificationsEnabled}, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId}
        """)
    int updateUserProfile(UserProfile userProfile);

    /**
     * 更新用户头像
     */
    @Update("UPDATE user_profiles SET avatar_url = #{avatarUrl}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateAvatar(String userId, String avatarUrl);

    /**
     * 更新用户显示名称
     */
    @Update("UPDATE user_profiles SET display_name = #{displayName}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateDisplayName(String userId, String displayName);

    /**
     * 更新用户偏好设置
     */
    @Update("""
        UPDATE user_profiles SET language = #{language}, timezone = #{timezone},
                                currency = #{currency}, notifications_enabled = #{notificationsEnabled},
                                updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId}
        """)
    int updatePreferences(String userId, String language, String timezone, String currency, Boolean notificationsEnabled);

    /**
     * 删除用户资料
     */
    @Delete("DELETE FROM user_profiles WHERE user_id = #{userId}")
    int deleteByUserId(String userId);
}