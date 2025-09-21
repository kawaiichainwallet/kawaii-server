package com.kawaiichainwallet.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Leaf分布式ID生成器配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LeafProperties.class)
public class LeafConfiguration {

    @Autowired
    private LeafProperties leafProperties;

    @Autowired
    private DataSource dataSource;

    /**
     * 简化版的Segment ID生成器
     * 注意：这是一个简化实现，生产环境建议使用完整的Leaf库
     */
    @Bean
    public SimpleSegmentIdGenerator segmentIdGenerator() {
        return new SimpleSegmentIdGenerator(dataSource);
    }

    /**
     * 简化版的Snowflake ID生成器
     */
    @Bean
    public SimpleSnowflakeIdGenerator snowflakeIdGenerator() {
        // 简化实现：使用服务端口作为机器ID的一部分
        long workerId = leafProperties.getSnowflake().getPort() % 1024;
        return new SimpleSnowflakeIdGenerator(workerId);
    }

    /**
     * 简化版Segment ID生成器实现
     */
    public static class SimpleSegmentIdGenerator {
        private final JdbcTemplate jdbcTemplate;

        public SimpleSegmentIdGenerator(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        public Long generate(String bizTag) {
            try {
                // 获取并更新ID段
                String updateSql = "UPDATE leaf_alloc SET max_id = max_id + step WHERE biz_tag = ?";
                int updated = jdbcTemplate.update(updateSql, bizTag);

                if (updated == 0) {
                    throw new RuntimeException("BizTag not found: " + bizTag);
                }

                // 获取更新后的max_id
                String selectSql = "SELECT max_id FROM leaf_alloc WHERE biz_tag = ?";
                Long maxId = jdbcTemplate.queryForObject(selectSql, Long.class, bizTag);

                return maxId;
            } catch (Exception e) {
                log.error("Generate segment ID failed for bizTag: {}", bizTag, e);
                throw new RuntimeException("Generate segment ID failed", e);
            }
        }
    }

    /**
     * 简化版Snowflake ID生成器实现
     * 基于Twitter Snowflake算法：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
     */
    public static class SimpleSnowflakeIdGenerator {
        private static final long EPOCH = 1640995200000L; // 2022-01-01 00:00:00
        private static final long WORKER_ID_BITS = 10L;
        private static final long SEQUENCE_BITS = 12L;
        private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
        private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

        private final long workerId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        public SimpleSnowflakeIdGenerator(long workerId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException(
                    String.format("Worker ID can't be greater than %d or less than 0", MAX_WORKER_ID));
            }
            this.workerId = workerId;
        }

        public synchronized Long generate() {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards");
            }

            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    timestamp = waitNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return ((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
                   (workerId << WORKER_ID_SHIFT) |
                   sequence;
        }

        private long waitNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }
}