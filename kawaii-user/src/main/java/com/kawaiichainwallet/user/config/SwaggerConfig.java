package com.kawaiichainwallet.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置类
 * 配置API文档
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KawaiiChain Wallet - User Service API")
                        .description("用户服务API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("KawaiiChain Team")
                                .email("contact@kawaiichainwallet.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8091/user")
                                .description("本地开发环境"),
                        new Server()
                                .url("http://localhost:8080/api/v1/user")
                                .description("通过网关访问")
                ));
    }
}