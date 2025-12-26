package com.nutcracker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * sc config server
 * 动态刷新配置文件命令：curl -X POST http://localhost:8888/actuator/refresh
 *
 * @author 胡桃夹子
 * @since 2025-12-02 10:17
 */
@Slf4j
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ConfigServerApplication.class, args);
        printApplicationInfo(context);
    }

    private static void printApplicationInfo(ApplicationContext ctx) {
        Environment env = ctx.getEnvironment();

        String appName = ctx.getId();
        String profile = String.join(", ", env.getActiveProfiles());
        String contextPath = env.getProperty("server.servlet.context-path", "");
        int port = env.getProperty("server.port", Integer.class, 8080);

        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }

        // 获取 BuildProperties（来自 spring-boot-maven-plugin build-info）
        String version = "UNKNOWN";
        try {
            BuildProperties bp = ctx.getBean(BuildProperties.class);
            version = bp.getVersion();
        } catch (Exception ignored) {
        }

        String baseUrl = "http://" + host + ":" + port + contextPath;

        log.info("""
                        
                        ------------------------------------------------------------
                         Application      : {}
                         Version          : {}
                         Active Profile   : {}
                         Context Path     : {}
                         Access URL       : {}
                        ------------------------------------------------------------
                        """,
                appName,
                version,
                profile.isBlank() ? "default" : profile,
                contextPath.isBlank() ? "/" : contextPath,
                baseUrl
        );
    }

}
