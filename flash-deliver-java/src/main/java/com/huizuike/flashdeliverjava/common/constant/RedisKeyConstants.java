package com.huizuike.flashdeliverjava.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants {

    private static final String SEPARATOR = ":";

    // 验证码相关
    public static final String SMS_CODE_PREFIX = "sms:code";
    public static final String SMS_CODE_LIMIT_PREFIX = "sms:limit";
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;
    public static final int SMS_CODE_LIMIT_SECONDS = 60;

    // Token 相关
    public static final String REFRESH_TOKEN_PREFIX = "refresh:token";
    public static final String REFRESH_TOKEN_BLACKLIST_PREFIX = "refresh:blacklist";

    public static String getSmsCodeKey(String phone) {
        return SMS_CODE_PREFIX + SEPARATOR + phone;
    }

    public static String getSmsCodeLimitKey(String phone) {
        return SMS_CODE_LIMIT_PREFIX + SEPARATOR + phone;
    }

    public static String getRefreshTokenKey(String token) {
        return REFRESH_TOKEN_PREFIX + SEPARATOR + token;
    }

    public static String getRefreshTokenBlacklistKey(String token) {
        return REFRESH_TOKEN_BLACKLIST_PREFIX + SEPARATOR + token;
    }
}