package com.huizuike.flashdeliverjava.common.context;

import com.huizuike.flashdeliverjava.pojo.entity.User;

/**
 * 用户上下文（基于 ThreadLocal 存储当前请求的用户信息）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setUserRole(String role) {
        USER_ROLE_THREAD_LOCAL.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getUserRole() {
        return USER_ROLE_THREAD_LOCAL.get();
    }

    /**
     * 清除上下文（请求结束后调用）
     */
    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
        USER_ROLE_THREAD_LOCAL.remove();
    }
}