package com.nutcracker.ttl.logging.mdc;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

/**
 *  MDC 工具类
 *
 * @author 胡桃夹子
 * @date 2025/12/22
 */
@UtilityClass
public class MdcSupport {

    public static final String TRACE_ID = "traceId";

    public static void put(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }
}
