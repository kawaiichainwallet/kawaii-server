package com.kawaiichainwallet.admin.init;

import com.kawaiichainwallet.admin.entity.AdminUser;
import com.kawaiichainwallet.admin.mapper.AdminUserMapper;
import com.kawaiichainwallet.admin.service.AdminIdGeneratorService;
import com.kawaiichainwallet.common.core.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 管理员账号初始化器
 * 在应用启动时自动初始化默认的超级管理员账号
 *
 * @author KawaiiChain
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final AdminIdGeneratorService idGeneratorService;
    private final PasswordEncoder passwordEncoder;

    // 默认超级管理员配置
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@kawaiichainwallet.com";
    private static final String DEFAULT_ADMIN_REAL_NAME = "系统管理员";

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== 开始检查管理员账号初始化 ===");

        // 检查是否已存在管理员账号
        AdminUser existingAdmin = adminUserMapper.findByIdentifier(DEFAULT_ADMIN_USERNAME);

        if (existingAdmin != null) {
            log.info("默认管理员账号已存在，跳过初始化");
            log.info("用户名: {}", existingAdmin.getUsername());
            log.info("邮箱: {}", existingAdmin.getEmail());
            return;
        }

        log.info("未发现默认管理员账号，开始初始化...");

        // 生成随机密码
        String randomPassword = generateRandomPassword();

        // 生成管理员ID
        Long adminId = idGeneratorService.generateAdminUserId();

        // 创建管理员实体
        AdminUser admin = new AdminUser();
        admin.setAdminId(adminId);
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setRealName(DEFAULT_ADMIN_REAL_NAME);
        admin.setPasswordHash(passwordEncoder.encode(randomPassword));
        admin.setStatus("active");
        admin.setIsSuperAdmin(true);
        admin.setTwoFactorEnabled(false);
        admin.setLoginAttempts(0);
        admin.setCreatedAt(TimeUtil.nowUtc());
        admin.setUpdatedAt(TimeUtil.nowUtc());
        admin.setCreatedBy(adminId); // 自己创建自己

        // 插入数据库
        int result = adminUserMapper.insert(admin);

        if (result > 0) {
            log.info("=== 默认管理员账号初始化成功 ===");
            log.info("用户名: {}", DEFAULT_ADMIN_USERNAME);
            log.info("邮箱: {}", DEFAULT_ADMIN_EMAIL);
            log.info("密码: {}", randomPassword);
            log.warn("!!! 重要：请立即登录并修改默认密码 !!!");
            log.info("========================================");
        } else {
            log.error("默认管理员账号初始化失败");
        }
    }

    /**
     * 生成随机密码
     * 使用安全随机数生成器，生成16字节的随机数据，然后Base64编码
     *
     * @return 随机密码字符串（约22个字符）
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);

        // 使用Base64编码（去掉末尾的=填充符，使用URL安全字符）
        String password = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        // 截取前16位，确保密码长度适中
        return password.substring(0, 16);
    }
}
