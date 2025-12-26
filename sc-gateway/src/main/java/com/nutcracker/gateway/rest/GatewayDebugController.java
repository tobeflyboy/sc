package com.nutcracker.gateway.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 网关调试控制器,仅在 dev 配置文件激活时生效
 *
 * @author 胡桃夹子
 * @date 2025/12/25
 */
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@RestController
public class GatewayDebugController {

    private final GatewayProperties gatewayProperties;

    @GetMapping("/gateway/routes")
    public Flux<String> routes() {
        log.info("GatewayDebugController.routes()");
        return Flux.fromIterable(gatewayProperties.getRoutes())
                .map(r -> String.format("RouteDefinition: id=%s, uri=%s, predicates=%s, filters=%s",
                        r.getId(), r.getUri(), r.getPredicates(), r.getFilters()))
                .doOnNext(log::info);
    }
}
