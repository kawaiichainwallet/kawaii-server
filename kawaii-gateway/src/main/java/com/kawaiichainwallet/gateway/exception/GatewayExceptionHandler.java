package com.kawaiichainwallet.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawaiichainwallet.common.core.enums.ApiCode;
import com.kawaiichainwallet.common.core.exception.BusinessException;
import com.kawaiichainwallet.common.core.response.R;
import com.kawaiichainwallet.common.spring.config.ObjectMapperFactory;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Gateway WebFlux 全局异常处理器
 * Order设为-2，优先级高于DefaultErrorWebExceptionHandler(-1)
 */
@Slf4j
@Order(-2)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler() {
        this.objectMapper = ObjectMapperFactory.createObjectMapper();
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
        if (ex instanceof BusinessException businessException) {
            // 处理业务异常
            httpStatus = getHttpStatusFromCode(businessException.getCode());
            result = R.error(businessException.getCode(), businessException.getMessage());
            log.warn("业务异常: code={}, message={}", businessException.getCode(), businessException.getMessage());
        } else if (ex instanceof ResponseStatusException statusException) {
            httpStatus = HttpStatus.valueOf(statusException.getStatusCode().value());
            result = handleResponseStatusException(statusException);
        } else if (ex instanceof IllegalArgumentException) {
            httpStatus = HttpStatus.BAD_REQUEST;
            result = R.error(ApiCode.BAD_REQUEST, ex.getMessage());
            log.warn("非法参数: {}", ex.getMessage());
        } else if (ex.getClass().getSimpleName().contains("AccessDenied")) {
            httpStatus = HttpStatus.FORBIDDEN;
            result = R.error(ApiCode.FORBIDDEN, "权限不足");
            log.warn("访问被拒绝: {}", ex.getMessage());
        } else if (ex.getClass().getSimpleName().contains("Authentication")) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            result = R.error(ApiCode.UNAUTHORIZED, "认证失败");
            log.warn("认证失败: {}", ex.getMessage());
        } else if (ex instanceof TimeoutException || ex.getClass().getSimpleName().contains("Timeout")) {
            // 超时异常
            httpStatus = HttpStatus.GATEWAY_TIMEOUT;
            result = R.error(ApiCode.GATEWAY_TIMEOUT, "后端服务响应超时");
            log.warn("后端服务超时: {}", ex.getMessage());
        } else if (ex.getCause() instanceof IOException || ex.getClass().getSimpleName().contains("ConnectException")) {
            // 连接异常
            httpStatus = HttpStatus.BAD_GATEWAY;
            result = R.error(ApiCode.BAD_GATEWAY, "后端服务不可用");
            log.error("后端服务连接失败: {}", ex.getMessage(), ex);
        } else if (ex.getClass().getSimpleName().contains("ServiceUnavailable")) {
            // 服务不可用
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            result = R.error(ApiCode.SERVICE_UNAVAILABLE, "服务暂时不可用");
            log.error("服务不可用: {}", ex.getMessage());
        } else {
            // 未知异常
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            result = R.error(ApiCode.INTERNAL_SERVER_ERROR, "网关内部错误");
            log.error("网关未知异常", ex);
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

    /**
     * 根据业务错误码获取HTTP状态码
     */
    private HttpStatus getHttpStatusFromCode(Integer code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // 2xx 成功
        if (code >= 200 && code < 300) {
            return HttpStatus.OK;
        }

        // 4xx 客户端错误
        if (code >= 400 && code < 500) {
            return switch (code) {
                case 400 -> HttpStatus.BAD_REQUEST;
                case 401 -> HttpStatus.UNAUTHORIZED;
                case 403 -> HttpStatus.FORBIDDEN;
                case 404 -> HttpStatus.NOT_FOUND;
                case 405 -> HttpStatus.METHOD_NOT_ALLOWED;
                case 409 -> HttpStatus.CONFLICT;
                case 429 -> HttpStatus.TOO_MANY_REQUESTS;
                default -> HttpStatus.BAD_REQUEST;
            };
        }

        // 5xx 服务端错误
        if (code >= 500 && code < 600) {
            return switch (code) {
                case 503 -> HttpStatus.SERVICE_UNAVAILABLE;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
        }

        // 业务错误码(1xxx-9xxx)，统一返回200状态码，由前端根据code判断
        return HttpStatus.OK;
    }
}