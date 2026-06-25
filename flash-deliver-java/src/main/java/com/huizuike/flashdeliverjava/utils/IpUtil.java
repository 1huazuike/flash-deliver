package com.huizuike.flashdeliverjava.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpUtil {

    /**
     * 获取客户端真实 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        // 1. X-Forwarded-For（最常用，经过代理时）
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 多个代理时取第一个（最原始 IP）
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }

        // 2. X-Real-IP（Nginx 常用）
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 3. Proxy-Client-IP（Apache 代理）
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 4. WL-Proxy-Client-IP（WebLogic 代理）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 5. 最后从 RemoteAddr 获取（直连时）
        ip = request.getRemoteAddr();

        // 本地测试时返回固定值
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    private static boolean isValidIp(String ip) {
        return ip != null
                && !ip.isEmpty()
                && !"unknown".equalsIgnoreCase(ip)
                && !"null".equalsIgnoreCase(ip);
    }
}