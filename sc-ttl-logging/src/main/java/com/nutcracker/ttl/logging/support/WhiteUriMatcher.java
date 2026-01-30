package com.nutcracker.ttl.logging.support;

import java.util.List;

import org.springframework.util.AntPathMatcher;

import lombok.NonNull;

/**
 * 白名单URI匹配器
 *
 * @author 胡桃夹子
 * @date 2025/12/23
 */
public final class WhiteUriMatcher {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private WhiteUriMatcher() {
    }

    public static boolean match(@NonNull String uri, List<String> whiteUris) {
        if (whiteUris == null || whiteUris.isEmpty()) {
            return false;
        }
        for (String pattern : whiteUris) {
            if (pattern != null && MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
