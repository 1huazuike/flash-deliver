package com.huizuike.flashdeliverjava.common.interceptor;

import com.huizuike.flashdeliverjava.common.context.UserContext;
import com.huizuike.flashdeliverjava.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    /**
     * 白名单路径（不需要登录验证）
     */
    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/sms-code",
            "/api/doc.html",
            "/api/swagger-ui/**",
            "/api/v3/api-docs/**",
            "/api/webjars/**",
            "/api/actuator/**",
            "/api/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单检查
        if (isWhiteList(path)) {
            return true;
        }

        // 刷新 Token 接口放行（单独处理）
        if (path.contains("/auth/refresh")) {
            return true;
        }

        String token = getTokenFromRequest(request);
        if (token == null) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        // ✅ 只验证 Access Token
        if (!jwtUtil.validateAccessToken(token)) {
            // 判断是否过期
            if (jwtUtil.isTokenExpired(token)) {
                sendErrorResponse(response, 401, "Token已过期，请刷新");
            } else {
                sendErrorResponse(response, 401, "Token无效");
            }
            return false;
        }

        // 存入 ThreadLocal
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        UserContext.setUserId(userId);
        UserContext.setUserRole(role);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 从请求中获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从 Header 获取
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 从参数获取（WebSocket 等场景）
        String token = request.getParameter("token");
        if (token != null) {
            return token;
        }

        return null;
    }

    /**
     * 判断是否在白名单中
     */
    private boolean isWhiteList(String path) {
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");

        // 使用 Jackson 或手动构建 JSON
        String json = String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", code, message);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}