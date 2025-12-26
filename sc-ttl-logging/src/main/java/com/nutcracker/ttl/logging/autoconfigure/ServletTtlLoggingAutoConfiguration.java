package com.nutcracker.ttl.logging.autoconfigure;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.servlet.ServletRequestResponseLogFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * servlet-ttl日志自动配置
 *
 * @author 胡桃夹子
 * @date 2025/12/24
 */
@Slf4j
@AutoConfiguration(after = TtlLoggingAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletTtlLoggingAutoConfiguration {

    @Bean
    public FilterRegistrationBean<ServletRequestResponseLogFilter> ttlLoggingFilter(HttpLogProperties properties) {
        log.info("Servlet 模式下注册请求响应日志 Filter");
        FilterRegistrationBean<ServletRequestResponseLogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ServletRequestResponseLogFilter(properties));
        // ✅ URL Pattern
        bean.addUrlPatterns("/*");
        // ✅ 顺序：尽量靠前，但晚于 Spring Security
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        bean.setName("ttlLoggingFilter");
        return bean;
    }
}
