package com.nutcracker.ttl.logging.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 全局 TraceId 上下文管理
 *
 * @author 胡桃夹子
 * @date 2025/12/22
 */
public final class TraceContext {

    private static final TransmittableThreadLocal<String> TRACE_ID = new TransmittableThreadLocal<>();

    private TraceContext() {
    }

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void clear() {
        TRACE_ID.remove();
    }
}
