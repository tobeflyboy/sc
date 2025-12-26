package com.nutcracker.ttl.logging.autoconfigure;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.gateway.GatewayRequestResponseLogFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * 网关ttl日志自动配置
 *
 * @author 胡桃夹子
 * @date 2025/12/24
 */
@Slf4j
@AutoConfiguration(after = TtlLoggingAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.cloud.gateway.filter.GlobalFilter")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class GatewayTtlLoggingAutoConfiguration {

    @Bean
    public GatewayRequestResponseLogFilter gatewayRequestResponseLogFilter(HttpLogProperties properties) {
        log.info("Gateway (WebFlux) 模式下注册请求响应日志 GlobalFilter");
        return new GatewayRequestResponseLogFilter(properties);
    }
    //@Bean
    //public GatewayAllRequestLogWebFilter gatewayAllRequestLogWebFilter(HttpLogProperties properties) {
    //    log.info("Gateway (WebFlux) 模式下注册请求响应日志 WebFilter");
    //    return new GatewayAllRequestLogWebFilter(properties);
    //}
}
