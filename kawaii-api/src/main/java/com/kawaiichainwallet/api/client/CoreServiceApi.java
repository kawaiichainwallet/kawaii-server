package com.kawaiichainwallet.api.client;

import com.kawaiichainwallet.common.response.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核心钱包服务API接口定义
 * 该接口由核心服务实现，其他服务通过Feign调用
 */
@FeignClient(
    name = "kawaii-core",
    contextId = "coreServiceApi"
)
public interface CoreServiceApi {

    /**
     * 获取用户钱包余额
     */
    @GetMapping("/internal/wallets/{userId}/balance")
    R<WalletBalanceDto> getWalletBalance(@PathVariable("userId") String userId,
                                        @RequestParam(required = false) String coinType,
                                        @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 检查用户钱包是否存在
     */
    @GetMapping("/internal/wallets/{userId}/exists")
    R<Boolean> walletExists(@PathVariable("userId") String userId,
                           @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 创建用户钱包
     */
    @PostMapping("/internal/wallets")
    R<WalletDto> createWallet(@RequestBody CreateWalletRequest request,
                             @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 冻结/解冻钱包
     */
    @PutMapping("/internal/wallets/{userId}/freeze")
    R<Void> freezeWallet(@PathVariable("userId") String userId,
                        @RequestParam Boolean freeze,
                        @RequestParam String reason,
                        @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 获取交易历史
     */
    @GetMapping("/internal/wallets/{userId}/transactions")
    R<java.util.List<TransactionDto>> getTransactionHistory(
            @PathVariable("userId") String userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String coinType,
            @RequestHeader("X-Internal-Token") String internalToken);

    /**
     * 钱包余额DTO
     */
    class WalletBalanceDto {
        private String userId;
        private String coinType;
        private BigDecimal balance;
        private BigDecimal frozenBalance;
        private BigDecimal availableBalance;
        private LocalDateTime lastUpdated;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCoinType() { return coinType; }
        public void setCoinType(String coinType) { this.coinType = coinType; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public BigDecimal getFrozenBalance() { return frozenBalance; }
        public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }

        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    /**
     * 钱包DTO
     */
    class WalletDto {
        private String walletId;
        private String userId;
        private String address;
        private String coinType;
        private String status;
        private LocalDateTime createdAt;

        // Getters and Setters
        public String getWalletId() { return walletId; }
        public void setWalletId(String walletId) { this.walletId = walletId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCoinType() { return coinType; }
        public void setCoinType(String coinType) { this.coinType = coinType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * 创建钱包请求
     */
    class CreateWalletRequest {
        private String userId;
        private String coinType;
        private String walletType;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCoinType() { return coinType; }
        public void setCoinType(String coinType) { this.coinType = coinType; }

        public String getWalletType() { return walletType; }
        public void setWalletType(String walletType) { this.walletType = walletType; }
    }

    /**
     * 交易DTO
     */
    class TransactionDto {
        private String txId;
        private String userId;
        private String fromAddress;
        private String toAddress;
        private BigDecimal amount;
        private String coinType;
        private String status;
        private String txHash;
        private LocalDateTime createdAt;

        // Getters and Setters
        public String getTxId() { return txId; }
        public void setTxId(String txId) { this.txId = txId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getFromAddress() { return fromAddress; }
        public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

        public String getToAddress() { return toAddress; }
        public void setToAddress(String toAddress) { this.toAddress = toAddress; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCoinType() { return coinType; }
        public void setCoinType(String coinType) { this.coinType = coinType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getTxHash() { return txHash; }
        public void setTxHash(String txHash) { this.txHash = txHash; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}