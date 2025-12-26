package com.nutcracker.ttl.logging.autoconfigure;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 自动装配配置：根据 Web 类型自动注册 Filter / GlobalFilter
 *
 * @author 胡桃夹子
 * @date 2025/12/22
 */
@AutoConfiguration
@EnableConfigurationProperties(HttpLogProperties.class)
public class TtlLoggingAutoConfiguration {

}
