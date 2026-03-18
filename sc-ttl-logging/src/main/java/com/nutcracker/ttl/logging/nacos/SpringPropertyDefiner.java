package com.nutcracker.ttl.logging.nacos;

import org.springframework.core.env.Environment;

import ch.qos.logback.core.PropertyDefinerBase;
import lombok.extern.slf4j.Slf4j;

/**
 * 从Spring Environment获取属性值的自定义PropertyDefiner 用于在logback配置中支持Spring环境变量
 *
 * @author 胡桃夹子
 * @date 2023-12-20
 */
@Slf4j
public class SpringPropertyDefiner extends PropertyDefinerBase {

    // Spring Environment实例，由NacosLogbackConfigurator设置
    private static Environment environment;

    // 要获取的属性名
    private String name;

    // 默认值
    private String defaultValue;

    /**
     * 设置Spring Environment实例
     * 
     * @param env Spring Environment实例
     */
    public static void setEnvironment(Environment env) {
        environment = env;
        log.info("SpringPropertyDefiner initialized with Spring Environment");
    }

    /**
     * 设置要获取的属性名
     * 
     * @param name 属性名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置默认值
     * 
     * @param defaultValue 默认值
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPropertyValue() {
        if (environment == null) {
            log.warn("Spring Environment not initialized in SpringPropertyDefiner");
            return defaultValue;
        }

        if (name == null) {
            log.warn("Property name not set in SpringPropertyDefiner");
            return defaultValue;
        }

        String value = environment.getProperty(name, defaultValue);
        log.debug("SpringPropertyDefiner: {} = {}", name, value);
        return value;
    }
}
