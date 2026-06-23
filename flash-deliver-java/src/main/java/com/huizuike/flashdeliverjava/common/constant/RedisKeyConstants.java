package com.huizuike.flashdeliverjava.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants {

    private static final String SEPARATOR = ":";

    // 验证码
    public static final String SMS_CODE_PREFIX = "sms:code";

    // 验证码过期时间（5分钟）
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;

    /**
     * 获取验证码的 Redis Key
     */
    public static String getSmsCodeKey(String phone) {
        return SMS_CODE_PREFIX + SEPARATOR + phone;
    }
}