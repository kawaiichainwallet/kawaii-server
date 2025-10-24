package com.kawaiichainwallet.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

        /**
         * 最大可缓存的请求体大小：10MB
         * 超过此大小的请求体将不会被缓存，以避免内存溢出
         */
        private static final long MAX_CACHEABLE_BODY_SIZE = 10 * 1024 * 1024;

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

            // 检查Content-Length，超过限制则不缓存
            Long contentLength = request.getHeaders().getContentLength();
            if (contentLength != null && contentLength > MAX_CACHEABLE_BODY_SIZE) {
                log.debug("请求体过大，跳过缓存: {} bytes (max: {} bytes)",
                        contentLength, MAX_CACHEABLE_BODY_SIZE);
                return chain.filter(exchange);
            }

            // 使用Spring Cloud Gateway推荐的方式缓存请求体
            return ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders())
                    .bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> {
                        // 缓存请求体字符串
                        exchange.getAttributes().put(CUSTOM_CACHED_REQUEST_BODY_ATTR, body);

                        // 如果请求体为空，直接继续
                        if (body.isEmpty()) {
                            return chain.filter(exchange);
                        }

                        // 使用CachedBodyOutputMessage重新插入请求体
                        HttpHeaders headers = new HttpHeaders();
                        headers.putAll(request.getHeaders());
                        // 移除Content-Length，让BodyInserter自动设置
                        headers.remove(HttpHeaders.CONTENT_LENGTH);

                        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

                        return BodyInserters.fromValue(body)
                                .insert(outputMessage, new BodyInserterContext())
                                .then(Mono.defer(() -> {
                                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                                        @Override
                                        public HttpHeaders getHeaders() {
                                            HttpHeaders httpHeaders = new HttpHeaders();
                                            httpHeaders.putAll(super.getHeaders());
                                            // 设置正确的Content-Length
                                            if (httpHeaders.getContentLength() < 0) {
                                                httpHeaders.setContentLength(body.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
                                            }
                                            return httpHeaders;
                                        }

                                        @Override
                                        public Flux<DataBuffer> getBody() {
                                            return outputMessage.getBody();
                                        }
                                    };
                                    return chain.filter(exchange.mutate().request(decorator).build());
                                }));
                    });
        }

        @Override
        public int getOrder() {
            // 必须在RequestLoggingGlobalFilter之前执行
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }
    }
}
