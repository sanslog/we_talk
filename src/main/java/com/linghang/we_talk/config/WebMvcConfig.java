package com.linghang.we_talk.config;

import com.linghang.we_talk.filter.ArticleInterceptor;
import com.linghang.we_talk.filter.JwtInterceptor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    //TODO:使用spring security 实现鉴权

    private final JwtInterceptor jwtInterceptor; // 从容器中获取已注入依赖的实例

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
//                        "/auth/refresh",
                        "/auth/register",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/articles/{id}/view"
                );
    }
}

