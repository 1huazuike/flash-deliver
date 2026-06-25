package com.huizuike.flashdeliverjava.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsCodeService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发送验证码（带频率限制）
     */
    public void sendSmsCode(String phone) {
        String limitKey = RedisKeyConstants.getSmsCodeLimitKey(phone);
        String codeKey = RedisKeyConstants.getSmsCodeKey(phone);

        // 1. 检查频率限制
        String lastSendTime = stringRedisTemplate.opsForValue().get(limitKey);
        if (StrUtil.isNotBlank(lastSendTime)) {
            long lastTime = Long.parseLong(lastSendTime);
            long remainingSeconds = (lastTime + RedisKeyConstants.SMS_CODE_LIMIT_SECONDS * 1000 - System.currentTimeMillis()) / 1000;
            if (remainingSeconds > 0) {
                throw new RuntimeException("请求过于频繁，请等待 " + remainingSeconds + " 秒后重试");
            }
        }

        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 存储验证码
        stringRedisTemplate.opsForValue().set(
                codeKey,
                code,
                RedisKeyConstants.SMS_CODE_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        // 4. 记录发送时间
        stringRedisTemplate.opsForValue().set(
                limitKey,
                String.valueOf(System.currentTimeMillis()),
                RedisKeyConstants.SMS_CODE_LIMIT_SECONDS,
                TimeUnit.SECONDS
        );

        log.info("验证码发送成功，手机号: {}，验证码: {}", phone, code);
        // TODO: 实际调用短信服务商 API
    }

    /**
     * 校验验证码并删除（返回布尔值）
     */
    public boolean verifyAndDeleteSmsCode(String phone, String inputCode) {
        if (StrUtil.isBlank(inputCode)) {
            return false;
        }
        String key = RedisKeyConstants.getSmsCodeKey(phone);
        String redisCode = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(redisCode) || !redisCode.equals(inputCode)) {
            return false;
        }
        stringRedisTemplate.delete(key);
        return true;
    }
}