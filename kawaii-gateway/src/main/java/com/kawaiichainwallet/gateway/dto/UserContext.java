package com.kawaiichainwallet.gateway.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户上下文信息
 * 用于Gateway内部传递用户身份和权限信息
 */
@Data
@Builder
public class UserContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限列表（可选）
     */
    private List<String> permissions;

    /**
     * KYC等级（可选）
     */
    private String kycLevel;

    /**
     * 是否为内部请求
     */
    @Builder.Default
    private boolean internal = false;
}