package com.huizuike.flashdeliverjava.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import com.huizuike.flashdeliverjava.common.exception.BusinessException;
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
     * 发送验证码
     */
    public void sendSmsCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        String key = RedisKeyConstants.getSmsCodeKey(phone);

        stringRedisTemplate.opsForValue().set(
                key,
                code,
                RedisKeyConstants.SMS_CODE_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        log.info("验证码发送成功，手机号: {}，验证码: {}", phone, code);
        // TODO: 实际调用短信服务商 API
    }

    /**
     * 校验验证码（返回校验结果）
     */
    public boolean verifySmsCode(String phone, String inputCode) {
        if (StrUtil.isBlank(inputCode)) {
            return false;
        }

        String key = RedisKeyConstants.getSmsCodeKey(phone);
        String redisCode = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(redisCode)) {
            return false;
        }

        return redisCode.equals(inputCode);
    }

    /**
     * 校验验证码（抛出异常版本，用于业务逻辑中直接校验）
     */
    public void verifySmsCodeWithException(String phone, String inputCode, String errorMessage) {
        if (!verifySmsCode(phone, inputCode)) {
            throw new BusinessException(400, errorMessage);
        }
    }

    /**
     * 校验验证码并删除（验证通过后自动删除，确保一次性使用）
     */
    public boolean verifyAndDeleteSmsCode(String phone, String inputCode) {
        String key = RedisKeyConstants.getSmsCodeKey(phone);
        String redisCode = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(redisCode) || !redisCode.equals(inputCode)) {
            return false;
        }

        // 验证通过，删除验证码
        stringRedisTemplate.delete(key);
        return true;
    }

    /**
     * 校验验证码并删除（抛出异常版本）
     */
    public void verifyAndDeleteSmsCodeWithException(String phone, String inputCode, String errorMessage) {
        if (!verifyAndDeleteSmsCode(phone, inputCode)) {
            throw new BusinessException(400, errorMessage);
        }
    }

    /**
     * 删除验证码
     */
    public void deleteSmsCode(String phone) {
        String key = RedisKeyConstants.getSmsCodeKey(phone);
        stringRedisTemplate.delete(key);
    }
}