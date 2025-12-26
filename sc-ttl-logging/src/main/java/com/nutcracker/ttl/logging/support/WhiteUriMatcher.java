package com.nutcracker.ttl.logging.support;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 白名单URI匹配器
 *
 * @author 胡桃夹子
 * @date 2025/12/23
 */
public final class WhiteUriMatcher {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private WhiteUriMatcher() {}

    public static boolean match(String uri, List<String> whiteUris) {
        if (whiteUris == null || whiteUris.isEmpty()) {
            return false;
        }
        for (String pattern : whiteUris) {
            if (MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}
