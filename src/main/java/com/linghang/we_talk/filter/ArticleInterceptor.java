package com.linghang.we_talk.filter;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class ArticleInterceptor implements HandlerInterceptor {
    @Value("${redis-template.keys.article-views}")
    private String ARTICLE_VIEW;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String articleId =  request.getParameter("id");
        try {
            HashOperations<String, String, Long> hashOps = redisTemplate.opsForHash();
            hashOps.increment(ARTICLE_VIEW, articleId, 0L);
        } catch (Exception e) {
            log.error("增加文章浏览量出错{}", e.getMessage());
            //TODO:这里可以降级走数据库
        }
        return true;
    }

}
