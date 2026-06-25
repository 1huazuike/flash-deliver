package com.huizuike.flashdeliverjava.common.context;

import com.huizuike.flashdeliverjava.pojo.entity.User;

/**
 * 用户上下文（基于 ThreadLocal 存储当前用户）
 */
public class UserContext {

    private static final ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> IP_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(User user) {
        USER_THREAD_LOCAL.set(user);
    }

    public static User getUser() {
        return USER_THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    public static String getUserRole() {
        User user = getUser();
        return user != null ? user.getRole() : null;
    }

    public static String getUserPhone() {
        User user = getUser();
        return user != null ? user.getPhone() : null;
    }

    // ============ IP 信息（独立存储） ============
    public static void setClientIp(String ip) {
        IP_THREAD_LOCAL.set(ip);
    }

    public static String getClientIp() {
        return IP_THREAD_LOCAL.get();
    }

    // ============ 清理 ============
    public static void clear() {
        USER_THREAD_LOCAL.remove();
        IP_THREAD_LOCAL.remove();
    }
}