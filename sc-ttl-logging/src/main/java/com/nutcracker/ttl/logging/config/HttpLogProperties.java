package com.nutcracker.ttl.logging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 请求响应日志配置
 *
 * @author 胡桃夹子
 * @date 2025/12/22
 */
@Data
@ConfigurationProperties(prefix = "sc.logging.http")
public class HttpLogProperties {

    /**
     * 是否开启日志
     */
    private boolean enabled = false;

    /**
     * body 最大长度
     */
    private int maxLength = 2048;

    /**
     * 白名单 URI（支持 Ant Path）
     * 命中后：不走 TTL / MDC / 日志
     */
    private List<String> whiteUris = new ArrayList<>();
}