package com.kawaiichainwallet.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配角色请求DTO
 *
 * @author KawaiiChain
 */
@Data
public class AssignRolesRequest {

    /**
     * 管理员ID
     */
    @NotNull(message = "管理员ID不能为空")
    private Long adminId;

    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
