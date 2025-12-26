package com.nutcracker.ttl.logging.gateway;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.support.WhiteUriMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.StringJoiner;

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

        // 构建请求日志：方法 + URI + Header
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("GW REQ ")
                .append(exchange.getRequest().getMethod())
                .append(" ")
                .append(exchange.getRequest().getURI());

        HttpHeaders headers = exchange.getRequest().getHeaders();
        if (!headers.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ");
            headers.forEach((key, values) -> {
                if (values.size() == 1) {
                    joiner.add(key + "=" + values.getFirst());
                } else {
                    joiner.add(key + "=" + values);
                }
            });
            logBuilder.append(" Headers: ").append(joiner);
        }

        log.info(logBuilder.toString());


        return chain.filter(exchange)
                .doFinally(signalType -> {
                    var status = exchange.getResponse().getStatusCode();
                    log.info("GW RESP [{}] {} ({})", status != null ? status.value() : "UNKNOWN", exchange.getRequest().getURI(), signalType);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
