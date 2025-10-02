package com.kawaiichainwallet.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawaiichainwallet.gateway.config.RequestLoggingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestLoggingConfig loggingConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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

        // 在exchange中存储请求开始时间，用于计算耗时
        exchange.getAttributes().put("REQUEST_START_TIME", System.currentTimeMillis());
        exchange.getAttributes().put("REQUEST_ID", requestId);

        // 记录请求日志
        logRequest(request, requestId, startTime);

        // 装饰响应以记录响应日志
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        // 记录响应日志 - 使用AtomicReference来处理响应体内容
                        AtomicReference<DataBuffer> responseDataBuffer = new AtomicReference<>();
                        if (!dataBuffers.isEmpty()) {
                            responseDataBuffer.set(dataBuffers.get(0));
                            logResponse(exchange, requestId, responseDataBuffer.get());
                        }
                        return dataBuffers.get(0);
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
    private void logRequest(ServerHttpRequest request, String requestId, String startTime) {
        try {
            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("type", "REQUEST");
            logData.put("requestId", requestId);
            logData.put("timestamp", startTime);
            logData.put("method", request.getMethod().name());
            logData.put("uri", request.getURI().toString());
            logData.put("path", request.getPath().value());

            // 获取客户端IP
            String clientIp = getClientIp(request);
            logData.put("clientIp", clientIp);

            // 记录查询参数（脱敏处理）
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            if (!queryParams.isEmpty()) {
                logData.put("queryParams", maskSensitiveData(queryParams.toSingleValueMap()));
            }

            // 记录请求头（根据配置决定是否记录）
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

            // 记录Content-Type和Content-Length
            String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType != null) {
                logData.put("contentType", contentType);
            }

            String contentLength = request.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            if (contentLength != null) {
                logData.put("contentLength", contentLength);
            }

            // 如果是POST/PUT请求且内容类型是JSON，记录请求体（但要脱敏）
            // 注意：在WebFlux中读取请求体比较复杂，这里先记录基本信息

            log.info("API_REQUEST: {}", objectMapper.writeValueAsString(logData));

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

                    // 创建字节数组副本避免影响原数据流
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    // 保存当前读取位置，然后读取数据，最后恢复位置
                    int originalReaderIndex = dataBuffer.readPosition();
                    dataBuffer.read(bytes);
                    dataBuffer.readPosition(originalReaderIndex); // 恢复原始读取位置
                    String responseBody = new String(bytes, StandardCharsets.UTF_8);

                    // 脱敏处理响应体中的敏感信息
                    String maskedResponseBody = maskSensitiveJsonData(responseBody);
                    logData.put("responseBody", maskedResponseBody);
                }
            }

            log.info("API_RESPONSE: {}", objectMapper.writeValueAsString(logData));

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
            logData.put("clientIp", getClientIp(request));

            // 获取路由的目标服务
            URI routedUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            if (routedUri != null) {
                logData.put("targetService", routedUri.getHost() + ":" + routedUri.getPort());
            }

            log.info("API_COMPLETION: {}", objectMapper.writeValueAsString(logData));

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
            // 简单的字符串替换脱敏，避免解析复杂JSON
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
        // 设置较低的优先级，确保在其他过滤器之后执行
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}