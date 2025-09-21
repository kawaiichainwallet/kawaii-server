package com.kawaiichainwallet.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Leaf配置属性
 */
@Data
@ConfigurationProperties(prefix = "leaf")
public class LeafProperties {

    private Segment segment = new Segment();
    private Snowflake snowflake = new Snowflake();

    @Data
    public static class Segment {
        /**
         * 是否启用Segment模式
         */
        private boolean enable = true;

        /**
         * 数据库配置
         */
        private Jdbc jdbc = new Jdbc();

        @Data
        public static class Jdbc {
            private String url;
            private String username;
            private String password;
            private String driver;
        }
    }

    @Data
    public static class Snowflake {
        /**
         * 是否启用Snowflake模式
         */
        private boolean enable = true;

        /**
         * 服务端口（用于生成机器ID）
         */
        private int port = 8091;

        /**
         * ZooKeeper地址（兼容配置，实际使用Nacos）
         */
        private String zkAddress;

        /**
         * 是否使用Nacos替代ZooKeeper
         */
        private boolean useNacos = true;
    }
}