package com.linghang.we_talk.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET;
    @Value("${jwt.access-token-expire}")
    public long ACCESS_EXPIRE; // 30分钟
    @Value("${jwt.refresh-token-expire}")
    public long REFRESH_EXPIRE; // 7天

    public final String JWT_BLACKLIST = "jwt:blacklist:";
    public final String JWT_REFRESH = "jwt:refresh:";

    /**
     * 生成AccessToken
     */
    public String generateAccessToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRE))
                .sign(Algorithm.HMAC256(SECRET));
    }

     /**
      * 生成RefreshToken
     */
    public String generateRefreshToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRE))
                .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     *  验证Token
     */
    public String validateToken(String token) throws JWTVerificationException {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getSubject();
    }

    /**
     * 获取token剩余可用时间
     * */
    public long getRemainingTime(String oldAccessToken) {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(oldAccessToken)
                .getExpiresAt().getTime() - System.currentTimeMillis();
    }
}

