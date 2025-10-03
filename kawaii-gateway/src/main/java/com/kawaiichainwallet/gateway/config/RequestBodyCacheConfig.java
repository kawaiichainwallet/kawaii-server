package com.kawaiichainwallet.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 请求体缓存配置
 */
@Configuration
public class RequestBodyCacheConfig {

    /**
     * 自定义的请求体缓存属性键
     */
    public static final String CUSTOM_CACHED_REQUEST_BODY_ATTR = "cachedRequestBodyString";

    /**
     * 请求体缓存全局过滤器
     */
    @Slf4j
    @Component
    public static class CacheRequestBodyFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            HttpMethod method = request.getMethod();

            // 只缓存POST和PUT请求的请求体
            if (method != HttpMethod.POST && method != HttpMethod.PUT) {
                return chain.filter(exchange);
            }

            // 检查是否是JSON请求
            String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
                return chain.filter(exchange);
            }

            // 使用Spring Cloud Gateway的ServerRequest来读取请求体
            return ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders())
                .bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> {
                    // 将请求体存储到exchange attribute中
                    exchange.getAttributes().put(CUSTOM_CACHED_REQUEST_BODY_ATTR, body);

                    // 如果请求体为空，直接继续
                    if (body.isEmpty()) {
                        return chain.filter(exchange);
                    }

                    // 创建新的请求装饰器，重新插入请求体
                    ServerHttpRequest decorator = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            // 使用DataBufferFactory创建新的DataBuffer
                            DataBuffer buffer = exchange.getResponse().bufferFactory()
                                .wrap(body.getBytes(StandardCharsets.UTF_8));
                            return Flux.just(buffer);
                        }
                    };

                    // 使用装饰后的请求继续执行过滤器链
                    return chain.filter(exchange.mutate().request(decorator).build());
                });
        }

        @Override
        public int getOrder() {
            // 必须在RequestLoggingGlobalFilter之前执行
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }
    }
}
