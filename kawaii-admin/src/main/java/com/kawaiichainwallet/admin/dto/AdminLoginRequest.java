package com.kawaiichainwallet.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求DTO
 *
 * @author KawaiiChain
 */
@Data
public class AdminLoginRequest {

    /**
     * 标识符 - 用户名或邮箱
     */
    @NotBlank(message = "请输入用户名或邮箱")
    private String identifier;

    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    private String password;
}
