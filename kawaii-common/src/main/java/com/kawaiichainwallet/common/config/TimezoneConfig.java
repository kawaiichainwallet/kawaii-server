package com.kawaiichainwallet.common.config;

import com.kawaiichainwallet.common.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * 时区配置类
 * 确保应用启动时设置正确的时区
 */
@Slf4j
@Configuration
public class TimezoneConfig {

    @Value("${app.timezone.jvm-timezone:UTC}")
    private String jvmTimezone;

    @PostConstruct
    public void init() {
        // 设置JVM默认时区为UTC
        TimeZone.setDefault(TimeZone.getTimeZone(jvmTimezone));

        log.info("应用时区配置已初始化:");
        log.info("  JVM默认时区: {}", TimeZone.getDefault().getID());
        log.info("  应用使用时区: {}", TimeUtil.UTC_ZONE.getId());
        log.info("  当前UTC时间: {}", TimeUtil.formatToIso(TimeUtil.nowInstant()));

        // 验证时区设置是否正确
        if (!TimeZone.getDefault().getID().equals(jvmTimezone)) {
            log.warn("警告: JVM时区设置可能未生效。期望: {}, 实际: {}",
                    jvmTimezone, TimeZone.getDefault().getID());
        }
    }
}