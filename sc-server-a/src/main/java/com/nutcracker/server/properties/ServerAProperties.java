package com.nutcracker.server.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 服务器特性
 *
 * @author 胡桃夹子
 * @date 2025/12/26
 */
@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "custom")
@Component
public class ServerAProperties {

    private String greetingMessage;

}
