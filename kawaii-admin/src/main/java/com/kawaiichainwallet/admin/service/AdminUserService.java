package com.kawaiichainwallet.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kawaiichainwallet.admin.dto.AdminUserDto;
import com.kawaiichainwallet.admin.dto.CreateAdminRequest;
import com.kawaiichainwallet.admin.dto.UpdateAdminRequest;
import com.kawaiichainwallet.admin.entity.AdminRole;
import com.kawaiichainwallet.admin.entity.AdminUser;
import com.kawaiichainwallet.admin.mapper.AdminUserMapper;
import com.kawaiichainwallet.admin.mapper.AdminUserRoleMapper;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.exception.BusinessException;
import com.kawaiichainwallet.common.core.utils.TimeUtil;
import com.kawaiichainwallet.common.core.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 管理员用户服务
 *
 * @author KawaiiChain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminIdGeneratorService idGeneratorService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询管理员列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态筛选（可选）
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    public IPage<AdminUserDto> getAdminUsers(int pageNum, int pageSize, String status, String keyword) {
        Page<AdminUser> page = new Page<>(pageNum, pageSize);
        IPage<AdminUser> result = adminUserMapper.selectPageWithSearch(page, status, keyword);

        // 转换为DTO
        return result.convert(this::convertToDto);
    }

    /**
     * 根据ID获取管理员详情
     *
     * @param adminId 管理员ID
     * @return 管理员详情
     */
    public AdminUserDto getAdminById(Long adminId) {
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND, "管理员不存在");
        }

        return convertToDto(admin);
    }

    /**
     * 创建管理员
     *
     * @param request 创建请求
     * @param createdBy 创建人ID
     * @return 管理员详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminUserDto createAdmin(CreateAdminRequest request, Long createdBy) {
        // 验证用户名是否已存在
        if (adminUserMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "用户名已存在");
        }

        // 验证邮箱是否已存在
        if (adminUserMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "邮箱已存在");
        }

        // 如果提供了手机号，验证是否已存在
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (adminUserMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException(ApiCode.VALIDATION_ERROR, "手机号已存在");
            }
        }

        // 生成管理员ID
        Long adminId = idGeneratorService.generateAdminUserId();

        // 创建管理员实体
        AdminUser admin = new AdminUser();
        admin.setAdminId(adminId);
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setRealName(request.getRealName());
        admin.setEmployeeId(request.getEmployeeId());
        admin.setDepartment(request.getDepartment());
        admin.setPosition(request.getPosition());
        admin.setStatus("active");
        admin.setTwoFactorEnabled(false);
        admin.setIsSuperAdmin(request.getIsSuperAdmin() != null ? request.getIsSuperAdmin() : false);
        admin.setPermissions(request.getPermissions());
        admin.setLoginAttempts(0);
        admin.setCreatedAt(TimeUtil.nowUtc());
        admin.setUpdatedAt(TimeUtil.nowUtc());
        admin.setCreatedBy(createdBy);

        // 设置密码（如果提供了密码则使用，否则生成随机密码）
        String password = request.getPassword();
        if (password == null || password.isEmpty()) {
            password = generateRandomPassword();
            log.info("为管理员生成随机密码: adminId={}", adminId);
        }
        admin.setPasswordHash(passwordEncoder.encode(password));

        // 保存管理员
        adminUserMapper.insert(admin);

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            adminUserRoleMapper.batchInsert(adminId, request.getRoleIds(), createdBy);
        }

        log.info("创建管理员成功: adminId={}, username={}, createdBy={}",
                adminId, request.getUsername(), createdBy);

        return convertToDto(admin);
    }

    /**
     * 更新管理员信息
     *
     * @param adminId 管理员ID
     * @param request 更新请求
     * @param updatedBy 更新人ID
     * @return 管理员详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminUserDto updateAdmin(Long adminId, UpdateAdminRequest request, Long updatedBy) {
        // 查询管理员
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND, "管理员不存在");
        }

        // 更新邮箱（如果提供且与当前不同）
        if (request.getEmail() != null && !request.getEmail().equals(admin.getEmail())) {
            if (adminUserMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException(ApiCode.VALIDATION_ERROR, "邮箱已被使用");
            }
            admin.setEmail(request.getEmail());
        }

        // 更新手机号（如果提供且与当前不同）
        if (request.getPhone() != null && !request.getPhone().equals(admin.getPhone())) {
            if (adminUserMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException(ApiCode.VALIDATION_ERROR, "手机号已被使用");
            }
            admin.setPhone(request.getPhone());
        }

        // 更新其他字段
        if (request.getRealName() != null) {
            admin.setRealName(request.getRealName());
        }
        if (request.getEmployeeId() != null) {
            admin.setEmployeeId(request.getEmployeeId());
        }
        if (request.getDepartment() != null) {
            admin.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            admin.setPosition(request.getPosition());
        }

        admin.setUpdatedAt(TimeUtil.nowUtc());
        admin.setUpdatedBy(updatedBy);

        // 保存更新
        adminUserMapper.updateById(admin);

        log.info("更新管理员成功: adminId={}, updatedBy={}", adminId, updatedBy);

        return convertToDto(admin);
    }

    /**
     * 删除管理员
     *
     * @param adminId 管理员ID
     * @param deletedBy 删除人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Long adminId, Long deletedBy) {
        // 查询管理员
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND, "管理员不存在");
        }

        // 不能删除超级管理员
        if (Boolean.TRUE.equals(admin.getIsSuperAdmin())) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "不能删除超级管理员");
        }

        // 删除管理员
        adminUserMapper.deleteById(adminId);

        // 删除管理员的所有角色关联
        adminUserRoleMapper.deleteByAdminId(adminId);

        log.info("删除管理员成功: adminId={}, deletedBy={}", adminId, deletedBy);
    }

    /**
     * 修改管理员状态
     *
     * @param adminId 管理员ID
     * @param status 新状态
     * @param updatedBy 更新人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminStatus(Long adminId, String status, Long updatedBy) {
        // 验证状态值
        if (!status.equals("active") && !status.equals("inactive") && !status.equals("suspended")) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "无效的状态值");
        }

        // 查询管理员
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND, "管理员不存在");
        }

        // 不能停用超级管理员
        if (Boolean.TRUE.equals(admin.getIsSuperAdmin()) && !status.equals("active")) {
            throw new BusinessException(ApiCode.VALIDATION_ERROR, "不能停用超级管理员");
        }

        // 更新状态
        admin.setStatus(status);
        admin.setUpdatedAt(TimeUtil.nowUtc());
        admin.setUpdatedBy(updatedBy);
        adminUserMapper.updateById(admin);

        log.info("修改管理员状态: adminId={}, status={}, updatedBy={}", adminId, status, updatedBy);
    }

    /**
     * 分配角色给管理员
     *
     * @param adminId 管理员ID
     * @param roleIds 角色ID列表
     * @param assignedBy 分配人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long adminId, List<Long> roleIds, Long assignedBy) {
        // 查询管理员
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ApiCode.USER_NOT_FOUND, "管理员不存在");
        }

        // 删除现有角色
        adminUserRoleMapper.deleteByAdminId(adminId);

        // 分配新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            adminUserRoleMapper.batchInsert(adminId, roleIds, assignedBy);
        }

        log.info("分配角色成功: adminId={}, roleIds={}, assignedBy={}", adminId, roleIds, assignedBy);
    }

    /**
     * 转换为DTO
     */
    private AdminUserDto convertToDto(AdminUser admin) {
        AdminUserDto dto = new AdminUserDto();
        dto.setAdminId(admin.getAdminId());
        dto.setUsername(admin.getUsername());
        dto.setEmail(ValidationUtil.maskEmail(admin.getEmail()));
        dto.setPhone(admin.getPhone() != null ? ValidationUtil.maskPhone(admin.getPhone()) : null);
        dto.setRealName(admin.getRealName());
        dto.setEmployeeId(admin.getEmployeeId());
        dto.setDepartment(admin.getDepartment());
        dto.setPosition(admin.getPosition());
        dto.setStatus(admin.getStatus());
        dto.setTwoFactorEnabled(admin.getTwoFactorEnabled());
        dto.setIsSuperAdmin(admin.getIsSuperAdmin());
        dto.setPermissions(admin.getPermissions());
        dto.setLoginAttempts(admin.getLoginAttempts());
        dto.setLockedUntil(admin.getLockedUntil());
        dto.setLastLoginAt(admin.getLastLoginAt());
        dto.setLastLoginIp(admin.getLastLoginIp());
        dto.setCreatedAt(admin.getCreatedAt());
        dto.setUpdatedAt(admin.getUpdatedAt());
        dto.setCreatedBy(admin.getCreatedBy());
        dto.setUpdatedBy(admin.getUpdatedBy());

        // 查询角色
        List<AdminRole> roles = adminUserRoleMapper.findRolesByAdminId(admin.getAdminId());
        dto.setRoles(roles.stream().map(this::convertRoleToDto).collect(Collectors.toList()));

        return dto;
    }

    /**
     * 转换角色为DTO
     */
    private com.kawaiichainwallet.admin.dto.AdminRoleDto convertRoleToDto(AdminRole role) {
        com.kawaiichainwallet.admin.dto.AdminRoleDto dto = new com.kawaiichainwallet.admin.dto.AdminRoleDto();
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

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
