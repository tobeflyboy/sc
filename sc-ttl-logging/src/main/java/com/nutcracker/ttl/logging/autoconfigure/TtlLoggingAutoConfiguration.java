package com.nutcracker.ttl.logging.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.nacos.NacosLogbackConfigurator;

/**
 * 自动装配配置：根据 Web 类型自动注册 Filter / GlobalFilter
 *
 * @author 胡桃夹子
 * @date 2025/12/22
 */
@AutoConfiguration
@EnableConfigurationProperties({ HttpLogProperties.class })
public class TtlLoggingAutoConfiguration {

    @Bean
    @ConditionalOnClass(NacosConfigManager.class)
    public NacosLogbackConfigurator nacosLogbackConfigurator() {
        return new NacosLogbackConfigurator();
    }
}
