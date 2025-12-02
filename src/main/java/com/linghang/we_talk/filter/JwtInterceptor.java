package com.linghang.we_talk.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.linghang.we_talk.utils.JwtUtil;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private JwtUtil jwtUtil;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final String USERNAME_ATTR = "username";
    private static final String TOKEN_EXPIRED_HEADER = "X-Token-Expired";

    @Override
    public boolean preHandle(HttpServletRequest request, @Nullable HttpServletResponse response,@Nullable Object handler) {
        String token = request.getHeader(TOKEN_HEADER);

        if (redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + token)) {
            throw new JWTVerificationException("Invalid token");
        }

        try {
            request.setAttribute(USERNAME_ATTR, jwtUtil.validateToken(token));
            return true;
        } catch (TokenExpiredException e) {
            if (response != null) {
                response.setHeader(TOKEN_EXPIRED_HEADER, "true");
                response.setStatus(401);
            }
            return false;
        } catch (Exception e) {
            if (response != null) response.setStatus(401);
            return false;
        }
    }
}

