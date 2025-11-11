package com.kawaiichainwallet.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类
 * 提供密码编码器等安全相关的Bean
 *
 * 注意：Admin服务的认证由Gateway统一处理，这里只需要：
 * 1. 提供PasswordEncoder用于密码加密验证
 * 2. 禁用Spring Security的默认认证（因为Gateway已经处理）
 *
 * @author KawaiiChain
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器 - 使用BCrypt算法
     * BCrypt是一种自适应的哈希算法，专门设计用于密码存储
     *
     * 特点：
     * - 自动加盐
     * - 可调节计算复杂度
     * - 防止彩虹表攻击
     *
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     * Admin服务的认证由Gateway统一处理，这里禁用Spring Security的默认认证
     *
     * @param http HttpSecurity配置对象
     * @return 配置好的SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（因为使用JWT，不需要CSRF保护）
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用默认的登出
                .logout(AbstractHttpConfigurer::disable)
                // 设置Session管理为无状态（使用JWT）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置授权规则：允许所有请求（认证由Gateway处理）
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                );

        return http.build();
    }
}
