package com.kawaiichainwallet.common.business.notification;

import com.kawaiichainwallet.common.core.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 统一通知服务
 * 提供邮件、短信等通知功能的统一接口
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * 发送邮件通知
     */
    public void sendEmailNotification(String email, String subject, String content) {
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        // 这里可以集成实际的邮件发送服务
        log.info("EMAIL_NOTIFICATION | email={} | subject={} | timestamp={}", email, subject, timestamp);
        log.debug("Email content: {}", content);

        // TODO: 集成邮件发送服务，如SendGrid、阿里云邮件等
    }

    /**
     * 发送短信通知
     */
    public void sendSmsNotification(String phone, String content) {
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        // 这里可以集成实际的短信发送服务
        log.info("SMS_NOTIFICATION | phone={} | timestamp={}", phone, timestamp);
        log.debug("SMS content: {}", content);

        // TODO: 集成短信发送服务，如阿里云短信、腾讯云短信等
    }

    /**
     * 发送用户注册成功通知
     */
    public void sendRegistrationSuccessNotification(String email, String username) {
        String subject = "欢迎注册KawaiiChain钱包";
        String content = String.format("亲爱的%s，欢迎您注册KawaiiChain钱包！您的账户已经创建成功。", username);

        sendEmailNotification(email, subject, content);
    }

    /**
     * 发送登录通知
     */
    public void sendLoginNotification(String email, String username, String loginTime, String ipAddress) {
        String subject = "账户登录提醒";
        String content = String.format("您的账户%s于%s从IP地址%s登录。如非本人操作，请立即修改密码。",
                username, loginTime, ipAddress);

        sendEmailNotification(email, subject, content);
    }

    /**
     * 发送转账通知
     */
    public void sendTransferNotification(String email, String amount, String currency, String toAddress) {
        String subject = "转账确认通知";
        String content = String.format("您已成功向地址%s转账%s %s。", toAddress, amount, currency);

        sendEmailNotification(email, subject, content);
    }

    /**
     * 发送收款通知
     */
    public void sendReceiveNotification(String email, String amount, String currency, String fromAddress) {
        String subject = "收款确认通知";
        String content = String.format("您已从地址%s收到%s %s。", fromAddress, amount, currency);

        sendEmailNotification(email, subject, content);
    }

    /**
     * 发送支付成功通知
     */
    public void sendPaymentSuccessNotification(String email, String orderNo, String amount, String merchant) {
        String subject = "支付成功通知";
        String content = String.format("您在%s的订单%s支付成功，金额：%s。", merchant, orderNo, amount);

        sendEmailNotification(email, subject, content);
    }

    /**
     * 发送验证码
     */
    public void sendVerificationCode(String email, String phone, String code, String purpose) {
        if (email != null) {
            String subject = "验证码";
            String content = String.format("您的%s验证码是：%s，有效期5分钟。", purpose, code);
            sendEmailNotification(email, subject, content);
        }

        if (phone != null) {
            String content = String.format("【KawaiiChain】您的%s验证码是：%s，有效期5分钟。", purpose, code);
            sendSmsNotification(phone, content);
        }
    }

    /**
     * 发送安全警告
     */
    public void sendSecurityAlert(String email, String alertType, String details) {
        String subject = "安全警告";
        String content = String.format("检测到您的账户存在%s，详情：%s。如非本人操作，请立即联系客服。", alertType, details);

        sendEmailNotification(email, subject, content);
    }
}