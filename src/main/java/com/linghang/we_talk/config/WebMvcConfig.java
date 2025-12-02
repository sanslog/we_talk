package com.linghang.we_talk.config;

import com.linghang.we_talk.filter.ArticleInterceptor;
import com.linghang.we_talk.filter.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //TODO:使用spring security 实现鉴权
    @Resource
    private JwtInterceptor jwtInterceptor; // 从容器中获取已注入依赖的实例
    @Resource
    private ArticleInterceptor articleInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/auth/logout")    //开发中放开所有接口，保留logout测试接口。
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/refresh",
                        "/auth/register",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                );
    }
}

