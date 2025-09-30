package com.kawaiichainwallet.gateway.exception;

import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;

/**
 * Gateway WebFlux 全局异常处理器
 */
@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        R<?> result;
        HttpStatus httpStatus;

        // 根据异常类型处理
        if (ex instanceof ResponseStatusException statusException) {
            httpStatus = HttpStatus.valueOf(statusException.getStatusCode().value());
            result = handleResponseStatusException(statusException);
        } else if (ex instanceof IllegalArgumentException) {
            httpStatus = HttpStatus.BAD_REQUEST;
            result = R.error(ApiCode.BAD_REQUEST, ex.getMessage());
        } else if (ex.getClass().getSimpleName().contains("AccessDenied")) {
            httpStatus = HttpStatus.FORBIDDEN;
            result = R.error(ApiCode.FORBIDDEN, "权限不足");
            log.warn("Access denied in gateway: {}", ex.getMessage());
        } else if (ex.getClass().getSimpleName().contains("Authentication")) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            result = R.error(ApiCode.UNAUTHORIZED, "认证失败");
            log.warn("Authentication failed in gateway: {}", ex.getMessage());
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            result = R.error(ApiCode.INTERNAL_SERVER_ERROR, "网关内部错误");
            log.error("Gateway unexpected error occurred", ex);
        }

        response.setStatusCode(httpStatus);

        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing response", e);
            return Mono.error(e);
        }
    }

    private R<?> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : "请求处理失败";

        return switch (status) {
            case BAD_REQUEST -> R.error(ApiCode.BAD_REQUEST, message);
            case UNAUTHORIZED -> R.error(ApiCode.UNAUTHORIZED, message);
            case FORBIDDEN -> R.error(ApiCode.FORBIDDEN, message);
            case NOT_FOUND -> R.error(ApiCode.NOT_FOUND, message);
            case METHOD_NOT_ALLOWED -> R.error(ApiCode.METHOD_NOT_ALLOWED, message);
            case TOO_MANY_REQUESTS -> R.error(ApiCode.TOO_MANY_REQUESTS, message);
            case SERVICE_UNAVAILABLE -> R.error(ApiCode.SERVICE_UNAVAILABLE, message);
            default -> R.error(ApiCode.INTERNAL_SERVER_ERROR, message);
        };
    }
}