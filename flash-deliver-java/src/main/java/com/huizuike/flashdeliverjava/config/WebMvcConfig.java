package com.huizuike.flashdeliverjava.config;

import com.huizuike.flashdeliverjava.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")                    // 拦截所有 API
                .excludePathPatterns(
                        "/api/user/login",                     // 登录接口
                        "/api/user/register",                  // 注册接口
                        "/api/user/sms-code",                  // 发送验证码
                        "/api/doc.html",                       // Knife4j 文档
                        "/api/swagger-ui/**",                  // Swagger UI
                        "/api/v3/api-docs/**",                 // OpenAPI JSON
                        "/api/webjars/**",                     // WebJars 资源
                        "/api/actuator/**",                    // 健康检查
                        "/api/error"                           // 错误页面
                );
    }
}