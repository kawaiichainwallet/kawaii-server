package com.kawaiichainwallet.common.context;

import lombok.Data;
import java.util.List;

/**
 * 用户上下文信息
 * 从Gateway传递过来的用户信息
 */
@Data
public class UserContext {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 是否已认证
     */
    private boolean authenticated;

    /**
     * 请求来源
     */
    private String requestSource;

    /**
     * 请求时间戳
     */
    private Long requestTimestamp;
}