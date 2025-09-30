package com.kawaiichainwallet.common.business.audit;

import com.kawaiichainwallet.common.core.utils.TimeUtil;
import com.kawaiichainwallet.common.spring.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 操作审计服务
 * 记录用户关键操作
 */
@Slf4j
@Service
public class OperationAuditService {

    /**
     * 记录用户操作
     */
    public void logUserOperation(String operation, String details) {
        String userId = UserContextHolder.getCurrentUserId();
        String userEmail = UserContextHolder.getCurrentUserEmail();
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        // 这里可以扩展为写入数据库或发送到日志系统
        log.info("USER_OPERATION | userId={} | email={} | operation={} | details={} | timestamp={}",
                userId, userEmail, operation, details, timestamp);
    }

    /**
     * 记录认证事件
     */
    public void logAuthEvent(String userId, String email, String eventType, String details) {
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        log.info("AUTH_EVENT | userId={} | email={} | eventType={} | details={} | timestamp={}",
                userId, email, eventType, details, timestamp);
    }

    /**
     * 记录支付操作
     */
    public void logPaymentOperation(String userId, String operation, String amount, String currency, String details) {
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        log.info("PAYMENT_OPERATION | userId={} | operation={} | amount={} | currency={} | details={} | timestamp={}",
                userId, operation, amount, currency, details, timestamp);
    }

    /**
     * 记录钱包操作
     */
    public void logWalletOperation(String userId, String operation, String walletAddress, String details) {
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        log.info("WALLET_OPERATION | userId={} | operation={} | walletAddress={} | details={} | timestamp={}",
                userId, operation, walletAddress, details, timestamp);
    }

    /**
     * 记录系统事件
     */
    public void logSystemEvent(String eventType, String service, String details) {
        String timestamp = TimeUtil.formatToIso(TimeUtil.nowInstant());

        log.info("SYSTEM_EVENT | eventType={} | service={} | details={} | timestamp={}",
                eventType, service, details, timestamp);
    }
}