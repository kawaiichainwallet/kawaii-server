package com.kawaiichainwallet.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kawaiichainwallet.gateway.dto.ApiResponse;
import com.kawaiichainwallet.gateway.config.RouteSecurityConfig;
import com.kawaiichainwallet.gateway.dto.UserContext;
import com.kawaiichainwallet.gateway.service.JwtValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Gateway 认证过滤器工厂
 */
@Slf4j
@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    private RouteSecurityConfig routeSecurityConfig;

    @Autowired
    private JwtValidationService jwtValidationService;

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("Processing request to path: {}", path);

            // 1. 检查是否为公开端点
            if (routeSecurityConfig.isPublicPath(path)) {
                log.debug("Public endpoint access: {}", path);
                return chain.filter(exchange);
            }

            // 2. 检查是否为内部端点
            if (routeSecurityConfig.isInternalPath(path)) {
                return forbidden(exchange.getResponse(), "Internal endpoint not accessible externally");
            }

            // 3. 检查认证头
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange.getResponse(), "Missing or invalid authorization header");
            }

            // 4. 提取并验证 JWT Token
            String token = jwtValidationService.extractTokenFromHeader(authHeader);
            if (token == null || !jwtValidationService.validateAccessToken(token)) {
                return unauthorized(exchange.getResponse(), "Invalid or expired token");
            }

            // 5. 解析用户信息
            UserContext userContext = extractUserContextFromToken(token);
            if (userContext == null) {
                return unauthorized(exchange.getResponse(), "Failed to parse user information from token");
            }

            // 6. 检查管理员路径权限
            if (routeSecurityConfig.isAdminPath(path)) {
                if (!userContext.getRoles().contains("ADMIN")) {
                    return forbidden(exchange.getResponse(), "Admin access required");
                }
            }

            // 7. 检查角色权限
            List<String> requiredRoles = routeSecurityConfig.getRequiredRoles(path);
            if (!requiredRoles.isEmpty()) {
                boolean hasRequiredRole = requiredRoles.stream()
                        .anyMatch(role -> userContext.getRoles().contains(role));
                if (!hasRequiredRole) {
                    return forbidden(exchange.getResponse(),
                            String.format("Required role(s): %s", String.join(", ", requiredRoles)));
                }
            }

            // 8. 添加用户信息到请求头
            ServerHttpRequest modifiedRequest = request.mutate()
                    // 基础用户信息
                    .header("X-User-Id", userContext.getUserId())
                    .header("X-User-Email", userContext.getEmail())
                    .header("X-User-Roles", String.join(",", userContext.getRoles()))


                    // 请求追踪
                    .header("X-Request-Source", "gateway")
                    .header("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            log.debug("Authenticated request for user: {} with roles: {} to path: {}",
                    userContext.getUserId(), userContext.getRoles(), path);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }


    private UserContext extractUserContextFromToken(String token) {
        try {
            String userId = jwtValidationService.getUserIdFromToken(token);
            String username = jwtValidationService.getUsernameFromToken(token);

            // 根据用户信息构建用户上下文
            // 这里可以调用用户服务获取完整的用户信息，包括角色
            // 暂时使用基础信息和默认角色
            return UserContext.builder()
                    .userId(userId)
                    .email(username) // 如果username是邮箱格式
                    .roles(Arrays.asList("USER", "VERIFIED")) // 默认角色，实际应从用户服务获取
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract user context from token", e);
            return null;
        }
    }


    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        return writeErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> forbidden(ServerHttpResponse response, String message) {
        return writeErrorResponse(response, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        try {
            ApiResponse<?> apiResponse = ApiResponse.error(status.value(), message);
            String body = objectMapper.writeValueAsString(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing error response", e);
            return response.setComplete();
        }
    }

    public static class Config {
        // 配置属性（如果需要的话）
    }
}