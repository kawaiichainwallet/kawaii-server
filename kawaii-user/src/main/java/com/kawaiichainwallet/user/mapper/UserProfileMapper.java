package com.kawaiichainwallet.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kawaiichainwallet.user.entity.UserProfile;
import org.apache.ibatis.annotations.*;

/**
 * 用户资料数据访问接口
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 更新用户头像
     */
    @Update("UPDATE user_profiles SET avatar_url = #{avatarUrl}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateAvatar(@Param("userId") String userId, @Param("avatarUrl") String avatarUrl);

    /**
     * 更新用户显示名称
     */
    @Update("UPDATE user_profiles SET display_name = #{displayName}, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int updateDisplayName(@Param("userId") String userId, @Param("displayName") String displayName);

    /**
     * 更新用户偏好设置
     */
    @Update("""
        UPDATE user_profiles SET language = #{language}, timezone = #{timezone},
                                currency = #{currency}, notifications_enabled = #{notificationsEnabled},
                                updated_at = CURRENT_TIMESTAMP
        WHERE user_id = #{userId}
        """)
    int updatePreferences(@Param("userId") String userId,
                         @Param("language") String language,
                         @Param("timezone") String timezone,
                         @Param("currency") String currency,
                         @Param("notificationsEnabled") Boolean notificationsEnabled);
}