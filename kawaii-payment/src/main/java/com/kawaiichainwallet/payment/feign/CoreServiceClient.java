package com.kawaiichainwallet.payment.feign;

import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

/**
 * 钱包核心服务Feign客户端 - 支付服务调用
 */
@FeignClient(
    name = "kawaii-core",
    url = "${app.services.core.url:http://localhost:8083}"
)
public interface CoreServiceClient {

    /**
     * 获取用户钱包余额
     */
    @GetMapping("/api/v1/wallet/{userId}/balance")
    R<WalletBalance> getWalletBalance(@PathVariable("userId") String userId,
                                      @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 执行钱包转账
     */
    @PostMapping("/api/v1/wallet/transfer")
    R<TransferResult> executeTransfer(@RequestBody TransferRequest request,
                                      @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 钱包余额DTO
     */
    class WalletBalance {
        private String userId;
        private BigDecimal balance;
        private String currency;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    /**
     * 转账请求DTO
     */
    class TransferRequest {
        private String fromUserId;
        private String toUserId;
        private BigDecimal amount;
        private String currency;
        private String description;

        // Getters and Setters
        public String getFromUserId() { return fromUserId; }
        public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

        public String getToUserId() { return toUserId; }
        public void setToUserId(String toUserId) { this.toUserId = toUserId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 转账结果DTO
     */
    class TransferResult {
        private String transactionId;
        private String status;
        private String message;

        // Getters and Setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}