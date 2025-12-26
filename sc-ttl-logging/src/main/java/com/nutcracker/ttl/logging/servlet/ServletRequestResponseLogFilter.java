package com.nutcracker.ttl.logging.servlet;

import com.nutcracker.ttl.logging.config.HttpLogProperties;
import com.nutcracker.ttl.logging.context.TraceContext;
import com.nutcracker.ttl.logging.mdc.MdcSupport;
import com.nutcracker.ttl.logging.support.WhiteUriMatcher;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * servlet请求响应日志过滤器
 *
 * @author 胡桃夹子
 * @date 2025/12/24
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletRequestResponseLogFilter implements Filter {

    private final HttpLogProperties properties;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("ServletRequestResponseLogFilter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest rawReq) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // ⚠️ 先设置编码
        rawReq.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());


        // 白名单 URI 跳过
        if (WhiteUriMatcher.match(rawReq.getRequestURI(), properties.getWhiteUris())) {
            chain.doFilter(request, response);
            return;
        }

        // multipart/form-data 跳过
        String contentType = rawReq.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(rawReq, properties.getMaxLength());
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper((HttpServletResponse) response);

        // TTL + MDC TraceId
        String traceId = req.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        TraceContext.setTraceId(traceId);
        MdcSupport.put(traceId);

        try {
            chain.doFilter(req, resp);
        } finally {
            logRequest(req);
            logResponse(resp);

            TraceContext.clear();
            MdcSupport.clear();

            // 写回响应体
            resp.copyBodyToResponse();
        }
    }

    /** 打印请求信息，包括 header 和 body */
    private void logRequest(ContentCachingRequestWrapper req) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.append(name).append("=").append(req.getHeader(name)).append("; ");
        }

        byte[] cachedBody = req.getContentAsByteArray();
        String body = cachedBody.length == 0 ? "<empty>" : truncate(new String(cachedBody, StandardCharsets.UTF_8));

        log.info("REQ [{}] {} {} headers=[{}] body={}",
                TraceContext.getTraceId(), req.getMethod(), req.getRequestURI(), headers, body);
    }

    /** 打印响应信息，包括 header 和 body */
    private void logResponse(ContentCachingResponseWrapper resp) {
        StringBuilder headers = new StringBuilder();
        for (String name : resp.getHeaderNames()) {
            headers.append(name).append("=").append(resp.getHeader(name)).append("; ");
        }

        byte[] bodyBytes = resp.getContentAsByteArray();
        if (bodyBytes.length == 0) {
            log.info("RESP [{}] {} headers=[{}] body=<empty>", TraceContext.getTraceId(), resp.getStatus(), headers);
            return;
        }

        String encoding = resp.getHeader("Content-Encoding");
        String contentType = resp.getContentType();

        if (!isTextual(contentType)) {
            log.info("RESP [{}] {} headers=[{}] body=[{} bytes binary skipped]", TraceContext.getTraceId(),
                    resp.getStatus(), headers, bodyBytes.length);
            return;
        }

        try {
            String body;
            if ("gzip".equalsIgnoreCase(encoding)) {
                body = unGzip(bodyBytes);
            } else if ("deflate".equalsIgnoreCase(encoding)) {
                body = unDeflate(bodyBytes);
            } else if ("br".equalsIgnoreCase(encoding)) {
                log.info("RESP [{}] {} headers=[{}] body=[br compressed, skipped]", TraceContext.getTraceId(), resp.getStatus(), headers);
                return;
            } else {
                body = new String(bodyBytes, StandardCharsets.UTF_8);
            }
            body = truncate(body);
            log.info("RESP [{}] {} headers=[{}] body={}", TraceContext.getTraceId(), resp.getStatus(), headers, body);
        } catch (Exception ex) {
            log.warn("RESP [{}] {} headers=[{}] body=[decode failed: {}]", TraceContext.getTraceId(), resp.getStatus(), headers, ex.getMessage());
        }
    }

    private String unGzip(byte[] bytes) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            gis.transferTo(bos);
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private String unDeflate(byte[] bytes) throws IOException {
        try (InflaterInputStream iis = new InflaterInputStream(new ByteArrayInputStream(bytes));
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            iis.transferTo(bos);
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private boolean isTextual(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.contains("application/json")
                || contentType.contains("text")
                || contentType.contains("application/xml");
    }

    private String truncate(String body) {
        int max = properties.getMaxLength();
        if (body.length() <= max) {
            return body;
        }
        return body.substring(0, max) + "...";
    }

    @Override
    public void destroy() {
        log.info("ServletRequestResponseLogFilter destroy");
    }
}
