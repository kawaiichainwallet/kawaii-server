package com.kawaiichainwallet.common.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * UTC时间工具类
 * 确保整个应用使用统一的UTC时间处理
 */
public final class TimeUtil {

    /**
     * UTC时区
     */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    /**
     * ISO 8601 标准格式化器
     */
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * 简单日期格式化器
     */
    public static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtil() {
        // 工具类不允许实例化
    }

    /**
     * 获取当前UTC时间的Instant
     */
    public static Instant nowInstant() {
        return Instant.now();
    }

    /**
     * 获取当前UTC时间的LocalDateTime
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC_ZONE);
    }

    /**
     * 获取当前UTC时间的ZonedDateTime
     */
    public static ZonedDateTime nowZonedUtc() {
        return ZonedDateTime.now(UTC_ZONE);
    }

    /**
     * 将LocalDateTime转换为UTC时区的ZonedDateTime
     */
    public static ZonedDateTime toUtcZoned(LocalDateTime localDateTime) {
        return localDateTime.atZone(UTC_ZONE);
    }

    /**
     * 将Instant转换为UTC的LocalDateTime
     */
    public static LocalDateTime toUtcLocal(Instant instant) {
        return LocalDateTime.ofInstant(instant, UTC_ZONE);
    }

    /**
     * 将ZonedDateTime转换为UTC时区
     */
    public static ZonedDateTime toUtc(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(UTC_ZONE);
    }

    /**
     * 格式化为ISO 8601字符串（UTC时区）
     */
    public static String formatToIso(LocalDateTime localDateTime) {
        return toUtcZoned(localDateTime).format(ISO_FORMATTER);
    }

    /**
     * 格式化为ISO 8601字符串（UTC时区）
     */
    public static String formatToIso(Instant instant) {
        return instant.atZone(UTC_ZONE).format(ISO_FORMATTER);
    }

    /**
     * 格式化为简单字符串（UTC时区）
     */
    public static String formatToSimple(LocalDateTime localDateTime) {
        return localDateTime.format(SIMPLE_FORMATTER);
    }

    /**
     * 格式化为简单字符串（UTC时区）
     */
    public static String formatToSimple(Instant instant) {
        return toUtcLocal(instant).format(SIMPLE_FORMATTER);
    }

    /**
     * 解析ISO 8601字符串为Instant
     */
    public static Instant parseIsoToInstant(String isoString) {
        return ZonedDateTime.parse(isoString, ISO_FORMATTER).toInstant();
    }

    /**
     * 解析ISO 8601字符串为UTC LocalDateTime
     */
    public static LocalDateTime parseIsoToLocal(String isoString) {
        return ZonedDateTime.parse(isoString, ISO_FORMATTER)
                .withZoneSameInstant(UTC_ZONE)
                .toLocalDateTime();
    }

    /**
     * 检查时间是否已过期
     */
    public static boolean isExpired(Instant expirationTime) {
        return expirationTime.isBefore(nowInstant());
    }

    /**
     * 检查时间是否已过期
     */
    public static boolean isExpired(LocalDateTime expirationTime) {
        return expirationTime.isBefore(nowUtc());
    }

    /**
     * 计算两个时间点之间的秒数差
     */
    public static long secondsBetween(Instant start, Instant end) {
        return Duration.between(start, end).getSeconds();
    }

    /**
     * 计算两个时间点之间的秒数差
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).getSeconds();
    }

    /**
     * 添加秒数到时间
     */
    public static Instant plusSeconds(Instant instant, long seconds) {
        return instant.plusSeconds(seconds);
    }

    /**
     * 添加秒数到时间
     */
    public static LocalDateTime plusSeconds(LocalDateTime localDateTime, long seconds) {
        return localDateTime.plusSeconds(seconds);
    }
}