package com.nutcracker.ttl.logging.nacos;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import lombok.extern.slf4j.Slf4j;

/**
 * 从Nacos加载完整logback配置的自定义配置器 实现将整个logback-spring.xml配置文件从Nacos配置中心加载并应用
 * 支持配置变化监听和动态刷新
 *
 * @author 胡桃夹子
 * @date 2023-12-20
 */
@Slf4j
public class NacosLogbackConfigurator implements InitializingBean, DisposableBean {

    /**
     * Nacos配置管理器，用于获取配置服务
     */
    @Autowired
    private NacosConfigManager nacosConfigManager;

    /**
     * Nacos配置属性，用于获取默认配置信息
     */
    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    /**
     * Spring环境对象，用于获取环境变量
     */
    @Autowired
    private Environment environment;

    /**
     * logback配置在Nacos中的dataId
     */
    private String dataId;

    /**
     * Nacos中日志配置文件的分组
     */
    private String group;

    /**
     * 线程池资源，用于定期刷新日志配置
     */
    private ScheduledExecutorService executor;

    /**
     * 应用启动完成后执行的方法 初始化配置、加载日志配置并定时刷新
     */
    @Override
    public void afterPropertiesSet() {
        // 初始化配置信息
        initConfig(environment);

        // 设置SpringPropertyDefiner的环境变量，以便logback配置可以访问Spring环境属性
        SpringPropertyDefiner.setEnvironment(environment);

        // 从Nacos加载日志配置
        loadLogbackConfigFromNacos();

        // 定时刷新日志配置
        scheduleLogbackConfigRefresh();
    }

    /**
     * 初始化Nacos配置信息
     * 
     * @param env Spring环境对象
     */
    private void initConfig(Environment env) {
        this.dataId = env.getProperty("logging.nacos.dataId", "logback-spring.xml");
        this.group = nacosConfigProperties.getGroup();
    }

    /**
     * 从Nacos加载logback配置
     */
    private void loadLogbackConfigFromNacos() {
        try {
            ConfigService configService = nacosConfigManager.getConfigService();
            if (configService == null) {
                log.error("Failed to get ConfigService");
                return;
            }

            // 从Nacos获取完整的logback配置
            String logbackXml = configService.getConfig(dataId, group, 5000);
            if (logbackXml == null || logbackXml.isEmpty()) {
                log.error("Logback config not found in Nacos: dataId={}, group={}", dataId, group);
                return;
            }

            // 解析并应用logback配置
            applyLogbackConfig(logbackXml);
            log.info("Successfully loaded logback config from Nacos: dataId={}, group={}", dataId, group);

            // 监听配置变化
            configService.addListener(dataId, group, new com.alibaba.nacos.api.config.listener.Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("Logback config changed in Nacos, reloading: dataId={}, group={}", dataId, group);
                    applyLogbackConfig(configInfo);
                }

                @Override
                public Executor getExecutor() {
                    // 使用null表示使用默认线程池
                    return null;
                }
            });

        } catch (NacosException e) {
            log.error("Failed to load logback config from Nacos: dataId={}, group={}", dataId, group, e);
        }
    }

    /**
     * 应用logback配置
     * 
     * @param logbackXml 原始的logback配置XML
     */
    private void applyLogbackConfig(String logbackXml) {
        try {
            // 使用System.out而不是logger，防止日志循环
            log.info("Applying logback-spring config from Nacos...");

            // 在加载配置前，手动替换Spring环境变量
            String processedXml = replaceSpringProperties(logbackXml);

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();

            // 使用标准的JoranConfigurator来加载logback配置
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);

            try (InputStream inputStream = new ByteArrayInputStream(processedXml.getBytes("UTF-8"))) {
                configurator.doConfigure(inputStream);
            }

            loggerContext.start();
            log.info("Successfully applied logback-spring config from Nacos");

            // 配置完成后，可以使用logger输出信息
            LoggerFactory.getLogger(NacosLogbackConfigurator.class).info("Logback-spring config applied successfully from Nacos");

        } catch (Exception e) {
            log.error("Failed to apply logback-spring config from Nacos", e);
        }
    }

    /**
     * 手动替换Spring环境变量
     * 
     * @param logbackXml 原始的logback配置XML
     * @return 替换后的logback配置XML
     */
    private String replaceSpringProperties(String logbackXml) {
        // 从Spring Environment获取属性值
        String appName = environment.getProperty("spring.application.name", "application");
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        String logDir = environment.getProperty("logging.log-dir", "./logs");
        String maxHistory = environment.getProperty("logging.max-history", "7");
        String logstashAddress = environment.getProperty("logging.logstash-address", "10.28.21.213:4567");

        log.info("Replacing Spring properties in logback config:");
        log.info("  spring.application.name = {}", appName);
        log.info("  spring.profiles.active = {}", activeProfile);
        log.info("  logging.log-dir = {}", logDir);
        log.info("  logging.max-history = {}", maxHistory);
        log.info("  logging.logstash-address = {}", logstashAddress);

        // 手动替换配置中的变量
        return logbackXml.replace("${APP}", appName).replace("${ENV}", activeProfile).replace("${LOG_DIR}", logDir).replace("${MAX_HISTORY}", maxHistory).replace("${LOGSTASH_ADDRESS}", logstashAddress);
    }

    /**
     * 定时刷新日志配置 设置每60秒从Nacos重新加载一次logback配置
     */
    private void scheduleLogbackConfigRefresh() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "logback-config-refresh-thread");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(() -> {
            try {
                loadLogbackConfigFromNacos();
            } catch (Exception e) {
                log.error("Failed to refresh logback config", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * 销毁Bean时执行的方法 关闭线程池资源，防止线程泄漏
     */
    @Override
    public void destroy() {
        // 关闭线程池资源，防止线程泄漏
        if (executor != null && !executor.isShutdown()) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
                log.info("Successfully shutdown logback config refresh executor");
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("Failed to shutdown logback config refresh executor", e);
            }
        }
    }
}
