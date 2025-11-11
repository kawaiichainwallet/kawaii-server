package com.kawaiichainwallet.admin.service;

import com.kawaiichainwallet.admin.dto.AdminRoleDto;
import com.kawaiichainwallet.admin.dto.CreateRoleRequest;
import com.kawaiichainwallet.admin.dto.UpdateRoleRequest;
import com.kawaiichainwallet.admin.entity.AdminRole;
import com.kawaiichainwallet.admin.mapper.AdminRoleMapper;
import com.kawaiichainwallet.admin.mapper.AdminUserRoleMapper;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.exception.BusinessException;
import com.kawaiichainwallet.common.core.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员角色服务
 *
 * @author KawaiiChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final AdminRoleMapper adminRoleMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminIdGeneratorService idGeneratorService;

    /**
     * 获取所有激活的角色
     *
     * @return 角色列表
     */
    public List<AdminRoleDto> getAllActiveRoles() {
        List<AdminRole> roles = adminRoleMapper.findAllActive();
        return roles.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 获取所有角色（包括未激活的）
     *
     * @return 角色列表
     */
    public List<AdminRoleDto> getAllRoles() {
        List<AdminRole> roles = adminRoleMapper.selectList(null);
        return roles.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 根据ID获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    public AdminRoleDto getRoleById(Long roleId) {
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色不存在");
        }

        return convertToDto(role);
    }

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @param createdBy 创建人ID
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminRoleDto createRole(CreateRoleRequest request, Long createdBy) {
        // 验证角色编码是否已存在
        if (adminRoleMapper.existsByRoleCode(request.getRoleCode())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色编码已存在");
        }

        // 验证角色名称是否已存在
        if (adminRoleMapper.existsByRoleName(request.getRoleName())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色名称已存在");
        }

        // 生成角色ID
        Long roleId = idGeneratorService.generateRoleId();

        // 创建角色实体
        AdminRole role = new AdminRole();
        role.setRoleId(roleId);
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setPermissions(request.getPermissions());
        role.setMenuPermissions(request.getMenuPermissions());
        role.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        role.setCreatedAt(TimeUtil.nowUtc());
        role.setUpdatedAt(TimeUtil.nowUtc());
        role.setCreatedBy(createdBy);

        // 保存角色
        adminRoleMapper.insert(role);

        log.info("创建角色成功: roleId={}, roleCode={}, createdBy={}",
                roleId, request.getRoleCode(), createdBy);

        return convertToDto(role);
    }

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param request 更新请求
     * @param updatedBy 更新人ID
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminRoleDto updateRole(Long roleId, UpdateRoleRequest request, Long updatedBy) {
        // 查询角色
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色不存在");
        }

        // 不能修改预置角色的编码
        if (roleId >= 700001L && roleId <= 700005L) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "不能修改预置角色");
        }

        // 更新角色名称（如果提供且与当前不同）
        if (request.getRoleName() != null && !request.getRoleName().equals(role.getRoleName())) {
            if (adminRoleMapper.existsByRoleName(request.getRoleName())) {
                throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色名称已被使用");
            }
            role.setRoleName(request.getRoleName());
        }

        // 更新其他字段
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getPermissions() != null) {
            role.setPermissions(request.getPermissions());
        }
        if (request.getMenuPermissions() != null) {
            role.setMenuPermissions(request.getMenuPermissions());
        }
        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }

        role.setUpdatedAt(TimeUtil.nowUtc());
        role.setUpdatedBy(updatedBy);

        // 保存更新
        adminRoleMapper.updateById(role);

        log.info("更新角色成功: roleId={}, updatedBy={}", roleId, updatedBy);

        return convertToDto(role);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @param deletedBy 删除人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId, Long deletedBy) {
        // 查询角色
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色不存在");
        }

        // 不能删除预置角色
        if (roleId >= 700001L && roleId <= 700005L) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "不能删除预置角色");
        }

        // 检查是否有管理员使用该角色
        List<Long> adminIds = adminUserRoleMapper.findAdminIdsByRoleId(roleId);
        if (!adminIds.isEmpty()) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR,
                    String.format("该角色正在被 %d 个管理员使用，无法删除", adminIds.size()));
        }

        // 删除角色
        adminRoleMapper.deleteById(roleId);

        log.info("删除角色成功: roleId={}, deletedBy={}", roleId, deletedBy);
    }

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param isActive 是否激活
     * @param updatedBy 更新人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(Long roleId, boolean isActive, Long updatedBy) {
        // 查询角色
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "角色不存在");
        }

        // 不能停用预置角色
        if (roleId >= 700001L && roleId <= 700005L && !isActive) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "不能停用预置角色");
        }

        // 更新状态
        role.setIsActive(isActive);
        role.setUpdatedAt(TimeUtil.nowUtc());
        role.setUpdatedBy(updatedBy);
        adminRoleMapper.updateById(role);

        log.info("修改角色状态: roleId={}, isActive={}, updatedBy={}", roleId, isActive, updatedBy);
    }

    /**
     * 转换为DTO
     */
    private AdminRoleDto convertToDto(AdminRole role) {
        AdminRoleDto dto = new AdminRoleDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setDescription(role.getDescription());
        dto.setPermissions(role.getPermissions());
        dto.setMenuPermissions(role.getMenuPermissions());
        dto.setIsActive(role.getIsActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedBy(role.getUpdatedBy());
        return dto;
    }
}
