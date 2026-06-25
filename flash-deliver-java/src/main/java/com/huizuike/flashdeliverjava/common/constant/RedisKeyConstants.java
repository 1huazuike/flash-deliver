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

    public static String getSmsCodeKey(String phone) {
        return SMS_CODE_PREFIX + ":" + phone;
    }

    public static String getSmsCodeLimitKey(String phone) {
        return SMS_CODE_LIMIT_PREFIX + ":" + phone;
    }

    public static String getUserTokenKey(String token) {
        return USER_TOKEN_PREFIX + ":" + token;
    }
}