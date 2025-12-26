package com.nutcracker.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * sc gateway
 *
 * @author 胡桃夹子
 * @date 2022/6/2 08:50
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GatewayApplication.class, args);
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
                         API Doc          : {}/doc.html
                        ------------------------------------------------------------
                        """,
                appName,
                version,
                profile.isBlank() ? "default" : profile,
                contextPath.isBlank() ? "/" : contextPath,
                baseUrl,
                baseUrl
        );
    }


}
