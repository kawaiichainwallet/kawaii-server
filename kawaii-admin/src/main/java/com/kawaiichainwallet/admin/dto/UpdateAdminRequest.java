package com.kawaiichainwallet.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新管理员请求DTO
 *
 * @author KawaiiChain
 */
@Data
public class UpdateAdminRequest {

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 真实姓名
     */
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
}
