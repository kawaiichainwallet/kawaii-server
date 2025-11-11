package com.kawaiichainwallet.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建管理员请求DTO
 *
 * @author KawaiiChain
 */
@Data
public class CreateAdminRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 密码（管理员创建时可选，系统可自动生成）
     */
    @Size(min = 8, max = 50, message = "密码长度必须在8-50个字符之间")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 100, message = "真实姓名不能超过100个字符")
    private String realName;

    /**
     * 员工编号
     */
    @Size(max = 50, message = "员工编号不能超过50个字符")
    private String employeeId;

    /**
     * 部门
     */
    @Size(max = 100, message = "部门名称不能超过100个字符")
    private String department;

    /**
     * 职位
     */
    @Size(max = 100, message = "职位名称不能超过100个字符")
    private String position;

    /**
     * 是否超级管理员
     */
    private Boolean isSuperAdmin = false;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 额外权限列表
     */
    private List<String> permissions;
}
