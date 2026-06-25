package com.huizuike.flashdeliverjava.common.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import com.huizuike.flashdeliverjava.common.context.UserContext;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器（单拦截器完成 Token 校验 + 刷新）
 * 拦截所有 /api/** 请求，白名单路径放行
 */
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //  统一获取客户端 IP，存入 ThreadLocal
        String clientIp = IpUtil.getClientIp(request);
        UserContext.setClientIp(clientIp);

        // 1. 获取 Token
        String token = getTokenFromRequest(request);

        if (StrUtil.isBlank(token)) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        // 2. 从 Redis 查询用户
        String key = RedisKeyConstants.getUserTokenKey(token);
        String userJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(userJson)) {
            sendErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 3. 刷新 Token 有效期（30分钟）
        stringRedisTemplate.expire(key, RedisKeyConstants.TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 4. 将用户信息存入 ThreadLocal（使用 Hutool JSONUtil）
        User user = JSONUtil.toBean(userJson, User.class);
        UserContext.setUser(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // 从 Header 获取
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 从参数获取（WebSocket 等场景）
        return request.getParameter("token");
    }

    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"timestamp\":"
                + System.currentTimeMillis() + "}");
    }
}