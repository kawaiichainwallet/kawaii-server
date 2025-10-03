package com.kawaiichainwallet.common.core.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.kawaiichainwallet.common.core.exception.CryptoException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 */
@Slf4j
public class CryptoUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        return RandomUtil.randomString(32);
    }

    /**
     * 使用BCrypt加密密码
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 加密后的密码
     */
    public static String encryptPassword(String password, String salt) {
        if (StrUtil.isBlank(password) || StrUtil.isBlank(salt)) {
            throw new IllegalArgumentException("密码和盐值不能为空");
        }

        // 将盐值与密码结合
        String saltedPassword = password + salt;
        return BCrypt.hashpw(saltedPassword, BCrypt.gensalt(12));
    }

    /**
     * 验证密码
     *
     * @param password     原始密码
     * @param salt         盐值
     * @param hashedPassword 加密后的密码
     * @return 验证结果
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        if (StrUtil.isBlank(password) || StrUtil.isBlank(salt) || StrUtil.isBlank(hashedPassword)) {
            return false;
        }

        try {
            String saltedPassword = password + salt;
            return BCrypt.checkpw(saltedPassword, hashedPassword);
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }

    /**
     * 生成AES密钥
     */
    public static String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(256, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new CryptoException("生成AES密钥失败", e);
        }
    }

    /**
     * AES加密
     *
     * @param data 要加密的数据
     * @param key  密钥（Base64编码）
     * @return 加密后的数据（Base64编码）
     */
    public static String encryptAES(String data, String key) {
        if (StrUtil.isBlank(data) || StrUtil.isBlank(key)) {
            throw new IllegalArgumentException("数据和密钥不能为空");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new CryptoException("AES加密失败", e);
        }
    }

    /**
     * AES解密
     *
     * @param encryptedData 加密的数据（Base64编码）
     * @param key           密钥（Base64编码）
     * @return 解密后的数据
     */
    public static String decryptAES(String encryptedData, String key) {
        if (StrUtil.isBlank(encryptedData) || StrUtil.isBlank(key)) {
            throw new IllegalArgumentException("加密数据和密钥不能为空");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new CryptoException("AES解密失败", e);
        }
    }

    /**
     * 生成MD5哈希
     */
    public static String md5(String data) {
        if (StrUtil.isBlank(data)) {
            throw new IllegalArgumentException("数据不能为空");
        }
        return SecureUtil.md5(data);
    }

    /**
     * 生成SHA256哈希
     */
    public static String sha256(String data) {
        if (StrUtil.isBlank(data)) {
            throw new IllegalArgumentException("数据不能为空");
        }
        return SecureUtil.sha256(data);
    }

    /**
     * 生成随机字符串
     */
    public static String randomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        return RandomUtil.randomString(length);
    }

    /**
     * 生成数字验证码
     */
    public static String generateNumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        return RandomUtil.randomNumbers(length);
    }
}