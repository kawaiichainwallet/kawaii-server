package com.kawaiichainwallet.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kawaiichainwallet.user.entity.UserKyc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * KYC认证数据访问接口
 */
@Mapper
public interface UserKycMapper extends BaseMapper<UserKyc> {

    /**
     * 根据用户ID查询KYC信息
     */
    @Select("SELECT * FROM user_kyc WHERE user_id = #{userId}")
    UserKyc findByUserId(Long userId);
}
