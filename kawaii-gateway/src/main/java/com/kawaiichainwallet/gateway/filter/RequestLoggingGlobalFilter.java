package com.kawaiichainwallet.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawaiichainwallet.common.spring.config.ObjectMapperFactory;
import com.kawaiichainwallet.gateway.config.RequestBodyCacheConfig;
import com.kawaiichainwallet.gateway.config.RequestLoggingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 请求日志记录全局过滤器
 * 记录所有通过网关的请求和响应信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
    private final RequestLoggingConfig loggingConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 专用的API日志记录器
    private static final Logger apiLogger = LoggerFactory.getLogger("gateway.api.requests");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 检查是否启用日志记录
        if (!loggingConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 检查是否是排除的路径
        String path = request.getPath().value();
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // 生成请求ID用于追踪
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 获取并缓存客户端IP（避免在doFinally中访问已回收的request）
        String clientIp = getClientIp(request);

        // 在exchange中存储请求开始时间、ID和客户端IP，用于后续使用
        exchange.getAttributes().put("REQUEST_START_TIME", System.currentTimeMillis());
        exchange.getAttributes().put("REQUEST_ID", requestId);
        exchange.getAttributes().put("CLIENT_IP", clientIp);

        // 记录请求日志
        logRequest(exchange, requestId, startTime, clientIp);

        // 装饰响应以记录响应日志
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        // 合并所有buffer并记录日志
                        if (!dataBuffers.isEmpty()) {
                            // 合并多个buffer为一个
                            DataBuffer joinedBuffer = response.bufferFactory().join(dataBuffers);
                            // 记录响应日志
                            logResponse(exchange, requestId, joinedBuffer);
                            // 返回合并后的buffer
                            return joinedBuffer;
                        }
                        return response.bufferFactory().allocateBuffer(0);
                    }));
                }
                return super.writeWith(body);
            }
        };

        // 继续执行过滤器链
        return chain.filter(exchange.mutate().response(decoratedResponse).build())
            .doFinally(signalType -> {
                // 记录完成日志
                logRequestCompletion(exchange, requestId, signalType.toString());
            });
    }

    /**
     * 记录请求日志
     */
    private void logRequest(ServerWebExchange exchange, String requestId, String startTime, String clientIp) {
        ServerHttpRequest request = exchange.getRequest();
        try {
            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("type", "REQUEST");
            logData.put("requestId", requestId);
            logData.put("timestamp", startTime);
            logData.put("method", request.getMethod().name());
            logData.put("uri", request.getURI().toString());
            logData.put("path", request.getPath().value());

            // 使用传入的客户端IP
            logData.put("clientIp", clientIp);

            // 记录查询参数并脱敏
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            if (!queryParams.isEmpty()) {
                logData.put("queryParams", maskSensitiveData(queryParams.toSingleValueMap()));
            }

            // 记录请求头
            if (loggingConfig.isLogHeaders()) {
                HttpHeaders headers = request.getHeaders();
                Map<String, String> headerMap = new HashMap<>();
                headers.forEach((key, values) -> {
                    if (!values.isEmpty()) {
                        String value = isSensitiveField(key) ? maskValue(values.get(0)) : values.get(0);
                        headerMap.put(key.toLowerCase(), value);
                    }
                });
                logData.put("headers", headerMap);
            }

            // 记录User-Agent
            String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);
            if (userAgent != null) {
                logData.put("userAgent", userAgent);
            }

            // 记录内容类型和大小
            String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType != null) {
                logData.put("contentType", contentType);
            }

            String contentLength = request.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            if (contentLength != null) {
                logData.put("contentLength", contentLength);
            }

            // 读取缓存的请求体
            if (loggingConfig.isLogRequestBody() && isJsonRequest(request)) {
                String cachedBody = exchange.getAttribute(RequestBodyCacheConfig.CUSTOM_CACHED_REQUEST_BODY_ATTR);
                if (cachedBody != null && !cachedBody.isEmpty()) {
                    // 脱敏处理请求体
                    String maskedRequestBody = maskSensitiveJsonData(cachedBody);
                    logData.put("requestBody", maskedRequestBody);
                }
            }

            apiLogger.info("API_REQUEST: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("记录请求日志失败: requestId={}", requestId, e);
        }
    }

    /**
     * 记录响应日志
     */
    private void logResponse(ServerWebExchange exchange, String requestId, DataBuffer dataBuffer) {
        try {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute("REQUEST_START_TIME");
            long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("type", "RESPONSE");
            logData.put("requestId", requestId);
            logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logData.put("statusCode", response.getStatusCode() != null ? response.getStatusCode().value() : null);
            logData.put("duration", duration + "ms");

            // 记录响应头
            HttpHeaders responseHeaders = response.getHeaders();
            Map<String, String> headerMap = new HashMap<>();
            responseHeaders.forEach((key, values) -> {
                if (!values.isEmpty()) {
                    headerMap.put(key.toLowerCase(), values.get(0));
                }
            });
            logData.put("headers", headerMap);

            // 记录响应体大小
            if (dataBuffer != null) {
                int totalSize = dataBuffer.readableByteCount();
                logData.put("responseSize", totalSize + " bytes");

                // 根据配置决定是否记录响应体
                if (loggingConfig.isLogResponseBody() &&
                    totalSize <= loggingConfig.getMaxResponseBodySize() &&
                    isJsonResponse(response)) {

                    // 读取响应体内容
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    int originalReaderIndex = dataBuffer.readPosition();
                    dataBuffer.read(bytes);
                    dataBuffer.readPosition(originalReaderIndex);
                    String responseBody = new String(bytes, StandardCharsets.UTF_8);

                    // 脱敏处理响应体
                    String maskedResponseBody = maskSensitiveJsonData(responseBody);
                    logData.put("responseBody", maskedResponseBody);
                }
            }

            apiLogger.info("API_RESPONSE: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("记录响应日志失败: requestId={}", requestId, e);
        }
    }

    /**
     * 记录请求完成日志
     */
    private void logRequestCompletion(ServerWebExchange exchange, String requestId, String signalType) {
        try {
            Long startTime = exchange.getAttribute("REQUEST_START_TIME");
            long totalDuration = startTime != null ? System.currentTimeMillis() - startTime : -1;

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("type", "COMPLETION");
            logData.put("requestId", requestId);
            logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logData.put("method", request.getMethod().name());
            logData.put("path", request.getPath().value());
            logData.put("statusCode", response.getStatusCode() != null ? response.getStatusCode().value() : null);
            logData.put("totalDuration", totalDuration + "ms");
            logData.put("signalType", signalType);

            // 使用缓存的客户端IP（避免访问已回收的request）
            String clientIp = exchange.getAttribute("CLIENT_IP");
            logData.put("clientIp", clientIp != null ? clientIp : "unknown");

            // 获取路由的目标服务
            URI routedUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            if (routedUri != null) {
                logData.put("targetService", routedUri.getHost() + ":" + routedUri.getPort());
            }

            apiLogger.info("API_COMPLETION: {}", objectMapper.writeValueAsString(logData));

        } catch (Exception e) {
            log.error("记录完成日志失败: requestId={}", requestId, e);
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 判断是否是排除的路径
     */
    private boolean isExcludedPath(String path) {
        return loggingConfig.getExcludePaths().stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 判断是否是敏感字段
     */
    private boolean isSensitiveField(String fieldName) {
        return loggingConfig.getSensitiveFields().stream()
            .anyMatch(sensitive -> fieldName.toLowerCase().contains(sensitive.toLowerCase()));
    }

    /**
     * 判断是否是敏感路径
     */
    private boolean isSensitivePath(String path) {
        return loggingConfig.getSensitivePaths().stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 判断是否是JSON请求
     */
    private boolean isJsonRequest(ServerHttpRequest request) {
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    /**
     * 判断是否是JSON响应
     */
    private boolean isJsonResponse(ServerHttpResponse response) {
        String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    /**
     * 脱敏处理敏感数据
     */
    private Map<String, Object> maskSensitiveData(Map<String, String> data) {
        Map<String, Object> maskedData = new HashMap<>();
        data.forEach((key, value) -> {
            if (isSensitiveField(key)) {
                maskedData.put(key, maskValue(value));
            } else {
                maskedData.put(key, value);
            }
        });
        return maskedData;
    }

    /**
     * 脱敏处理值
     */
    private String maskValue(String value) {
        if (value == null || value.length() <= 4) {
            return "***";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    /**
     * 脱敏处理JSON数据
     */
    private String maskSensitiveJsonData(String jsonData) {
        try {
            // 使用正则表达式脱敏JSON字段
            String masked = jsonData;
            for (String sensitive : loggingConfig.getSensitiveFields()) {
                masked = masked.replaceAll(
                    "\"" + sensitive + "\"\\s*:\\s*\"[^\"]+\"",
                    "\"" + sensitive + "\":\"***\""
                );
            }
            return masked;
        } catch (Exception e) {
            return "[JSON解析失败]";
        }
    }

    @Override
    public int getOrder() {
        // 必须在请求体缓存过滤器之后执行
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}