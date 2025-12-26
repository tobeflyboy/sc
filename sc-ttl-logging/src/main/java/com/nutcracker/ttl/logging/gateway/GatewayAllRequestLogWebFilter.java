package com.nutcracker.ttl.logging.gateway;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.support.WhiteUriMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.StringJoiner;

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

        // 打印请求方法 + URI + Header 在同一条日志
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ALL REQ ")
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
                .doFinally(signal -> {
                    var status = exchange.getResponse().getStatusCode();
                    if (status == null || status.is4xxClientError() || status.is5xxServerError()) {
                        log.info("ALL RESP [{}] {} ({})",
                                status != null ? status.value() : "UNKNOWN",
                                exchange.getRequest().getURI(),
                                signal);
                    }
                });
    }

    @Override
    public int getOrder() {
        // 优先于 GlobalFilter
        return -100;
    }
}
