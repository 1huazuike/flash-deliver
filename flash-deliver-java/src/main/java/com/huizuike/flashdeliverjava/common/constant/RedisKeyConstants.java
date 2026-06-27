package com.huizuike.flashdeliverjava.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants {

    // ============ 验证码相关 ============
    public static final String SMS_CODE_PREFIX = "sms:code";
    public static final String SMS_CODE_LIMIT_PREFIX = "sms:limit";
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;
    public static final int SMS_CODE_LIMIT_SECONDS = 60;

    // ============ 用户登录 Token 相关 ============
    public static final String USER_TOKEN_PREFIX = "user:token";
    public static final int TOKEN_EXPIRE_SECONDS = 1800;  // 30分钟


    // ============ 多设备管理 ============
    public static final String USER_DEVICE_LIST_PREFIX = "user:device:list";
    public static final int MAX_DEVICE_COUNT = 3;  // 最多同时登录 3 台设备

    // ============ 登录安全（统一配置） ============
    public static final String LOGIN_FAIL_PREFIX = "login:fail";
    public static final String LOGIN_LOCK_PREFIX = "login:lock";
    public static final int LOGIN_FAIL_MAX = 10;                // 最多失败 10 次
    public static final int LOGIN_LOCK_SECONDS = 300;           // 锁定 5 分钟
    public static final int LOGIN_FAIL_WINDOW_SECONDS = 300;    // 5 分钟窗口

    // ============ Key 生成方法 ============

    // ============ Key 生成方法 ============

    /**
     * 获取验证码的 Redis Key
     */
    public static String getSmsCodeKey(String phone) {
        return SMS_CODE_PREFIX + ":" + phone;
    }

    /**
     * 获取验证码发送频率限制的 Redis Key
     */
    public static String getSmsCodeLimitKey(String phone) {
        return SMS_CODE_LIMIT_PREFIX + ":" + phone;
    }

    /**
     * 获取用户 Token 的 Redis Key
     */
    public static String getUserTokenKey(String token) {
        return USER_TOKEN_PREFIX + ":" + token;
    }

    /**
     * 获取用户设备列表的 Redis Key
     */
    public static String getUserDeviceListKey(Long userId) {
        return USER_DEVICE_LIST_PREFIX + ":" + userId;
    }

    /**
     * 获取登录失败记录的 Redis Key
     */
    public static String getLoginFailKey(String ip, String phone) {
        return LOGIN_FAIL_PREFIX + ":" + ip + ":" + phone;
    }

    /**
     * 获取登录锁定状态的 Redis Key
     */
    public static String getLoginLockKey(String ip, String phone) {
        return LOGIN_LOCK_PREFIX + ":" + ip + ":" + phone;
    }
}