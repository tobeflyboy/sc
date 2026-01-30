package com.nutcracker.ttl.logging.gateway;

import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.context.TraceContext;
import com.nutcracker.ttl.logging.mdc.MdcSupport;
import com.nutcracker.ttl.logging.support.WhiteUriMatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 网关所有请求日志web过滤器
 *
 * @author 胡桃夹子
 * @date 2025/12/24
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayAllRequestLogWebFilter implements WebFilter, Ordered {

    private final HttpLogProperties properties;

    @SuppressWarnings("null")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("uri={}", path);
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }

        // ✅ White URI 短路
        if (WhiteUriMatcher.match(path, properties.getWhiteUris())) {
            return chain.filter(exchange);
        }

        // 生成或获取TraceId
        final String traceIdHeader = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        String traceId = traceIdHeader == null || traceIdHeader.isBlank() ? UUID.randomUUID().toString() : traceIdHeader;

        // 设置TraceId到上下文
        TraceContext.setTraceId(traceId);
        MdcSupport.put(traceId);

        // 将TraceId添加到请求头中传递给下游服务
        final ServerHttpRequest request = exchange.getRequest().mutate().header("X-Trace-Id", traceId).build();
        final ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

        // 打印请求方法 + URI + Header 在同一条日志
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ALL REQ [").append(traceId).append("] ").append(request.getMethod()).append(" ").append(request.getURI());

        HttpHeaders headers = request.getHeaders();
        if (!headers.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ");
            headers.forEach((key, values) -> {
                if (values.size() == 1) {
                    joiner.add(key + "=" + values.get(0));
                } else {
                    joiner.add(key + "=" + values);
                }
            });
            logBuilder.append(" Headers: ").append(joiner);
        }

        log.info(logBuilder.toString());

        return chain.filter(mutatedExchange).doFinally(signal -> {
            // 将TraceId添加到响应头中返回给客户端
            ServerHttpResponse response = mutatedExchange.getResponse();
            response.getHeaders().add("X-Trace-Id", traceId);

            var status = response.getStatusCode();
            if (status == null || status.is4xxClientError() || status.is5xxServerError()) {
                log.info("ALL RESP [{}] [{}] {} ({})", traceId, status != null ? status.value() : "UNKNOWN", request.getURI(), signal);
            }

            // 清理上下文
            TraceContext.clear();
            MdcSupport.clear();
        });
    }

    @Override
    public int getOrder() {
        // 优先于 GlobalFilter
        return -100;
    }
}