package com.nutcracker.ttl.logging.gateway;

import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.context.TraceContext;
import com.nutcracker.ttl.logging.mdc.MdcSupport;
import com.nutcracker.ttl.logging.support.WhiteUriMatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway 请求响应日志打印 GlobalFilter
 *
 * @author 胡桃夹子
 * @date 2025/12/22
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayRequestResponseLogFilter implements GlobalFilter, Ordered {

    private final HttpLogProperties properties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
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
        final String traceId = traceIdHeader == null || traceIdHeader.isBlank() ? UUID.randomUUID().toString() : traceIdHeader;

        // 设置TraceId到上下文
        TraceContext.setTraceId(traceId);
        MdcSupport.put(traceId);

        // 将TraceId添加到请求头中传递给下游服务
        final ServerHttpRequest request = exchange.getRequest().mutate().header("X-Trace-Id", traceId).build();
        final ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

        // 构建请求日志：方法 + URI + Header
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("GW REQ [").append(traceId).append("] ").append(request.getMethod()).append(" ").append(request.getURI());

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

        return chain.filter(mutatedExchange).doFinally(signalType -> {
            // 将TraceId添加到响应头中返回给客户端
            ServerHttpResponse response = mutatedExchange.getResponse();
            response.getHeaders().add("X-Trace-Id", traceId);

            var status = response.getStatusCode();
            log.info("GW RESP [{}] [{}] {} ({})", traceId, status != null ? status.value() : "UNKNOWN", request.getURI(), signalType);

            // 清理上下文
            TraceContext.clear();
            MdcSupport.clear();
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}