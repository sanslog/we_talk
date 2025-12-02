package com.linghang.we_talk;

import com.linghang.we_talk.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testRedisTemplate() {
        // 测试RedisTemplate
        redisTemplate.opsForValue().set("test:key", "hello world");
        String value = redisTemplate.opsForValue().get("test:key");
        assertEquals("hello world", value);
    }

    @Test
    void testCacheAnnotation() {
        redisTemplate.opsForValue().increment("article:1");
        redisTemplate.opsForValue().increment("article:1");
        redisTemplate.opsForValue().increment("article:1");
        redisTemplate.opsForValue().increment("article:1");
        String s = redisTemplate.opsForValue().get("article:1");

        System.out.println(s);
    }
}
