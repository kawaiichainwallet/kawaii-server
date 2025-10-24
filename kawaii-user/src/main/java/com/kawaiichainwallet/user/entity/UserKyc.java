package com.kawaiichainwallet.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kawaiichainwallet.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * KYC认证实体类 - 对应 user_kyc 表
 *
 * <p><b>时间字段约定</b>：所有时间字段统一使用 UTC 时区</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_kyc")
public class UserKyc extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * KYC ID
     */
    @TableId(value = "kyc_id", type = IdType.INPUT)
    private Long kycId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 认证级别 (0-3)
     * 0: 未认证
     * 1: L1 基础认证
     * 2: L2 身份认证
     * 3: L3 高级认证
     */
    @TableField("kyc_level")
    private Integer kycLevel;

    /**
     * 认证状态
     */
    private String status;

    /**
     * 身份证件类型
     */
    @TableField("id_type")
    private String idType;

    /**
     * 身份证号（加密）
     */
    @TableField("id_number_encrypted")
    private byte[] idNumberEncrypted;

    /**
     * 证件正面URL
     */
    @TableField("id_front_url")
    private String idFrontUrl;

    /**
     * 证件背面URL
     */
    @TableField("id_back_url")
    private String idBackUrl;

    /**
     * 自拍照URL
     */
    @TableField("selfie_url")
    private String selfieUrl;

    /**
     * 提交时间（UTC）
     */
    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 审核时间（UTC）
     */
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 审核人ID
     */
    @TableField("reviewed_by")
    private Long reviewedBy;

    /**
     * 拒绝原因
     */
    @TableField("rejection_reason")
    private String rejectionReason;

    /**
     * 过期时间（UTC）
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 额外文档（JSON格式）
     */
    @TableField("additional_docs")
    private String additionalDocs;
}
