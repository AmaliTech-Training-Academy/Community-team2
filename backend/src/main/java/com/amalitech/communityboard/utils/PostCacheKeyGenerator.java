package com.amalitech.communityboard.utils;

import com.amalitech.communityboard.dto.request.PostFilter;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("postCacheKeyGenerator")
public class PostCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder key = new StringBuilder();
        for (Object param : params) {
            if (param instanceof PostFilter filter) {
                key.append("filter:")
                        .append(filter.getAuthorId()).append(":")
                        .append(filter.getCategoryId()).append(":")
                        .append(filter.getTitle()).append(":");
            } else if (param instanceof Pageable pageable) {
                key.append("page:")
                        .append(pageable.getPageNumber()).append(":")
                        .append(pageable.getPageSize()).append(":")
                        .append(pageable.getSort());
            }
        }
        return key.toString();
    }
}
