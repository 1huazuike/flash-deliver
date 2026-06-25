package com.huizuike.flashdeliverjava.config;

import com.huizuike.flashdeliverjava.common.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 白名单：不需要登录的接口
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/sms-code",
                        // Swagger 文档
                        "/api/doc.html",
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs/**",
                        "/api/webjars/**",
                        // 健康检查
                        "/api/actuator/**",
                        "/api/error"
                );
    }
}