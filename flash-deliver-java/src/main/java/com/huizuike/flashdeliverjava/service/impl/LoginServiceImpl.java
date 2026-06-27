package com.huizuike.flashdeliverjava.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import com.huizuike.flashdeliverjava.common.constant.UserConstants;
import com.huizuike.flashdeliverjava.common.context.UserContext;
import com.huizuike.flashdeliverjava.mapper.UserMapper;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.LoginDTO;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import com.huizuike.flashdeliverjava.pojo.vo.UserVO;
import com.huizuike.flashdeliverjava.service.user.LoginService;
import com.huizuike.flashdeliverjava.utils.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants.*;
import static com.huizuike.flashdeliverjava.common.constant.UserConstants.IS_DELETED_NO;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService {

    private final UserMapper userMapper;
    private final SmsCodeService smsCodeService;
    private final StringRedisTemplate stringRedisTemplate;

    // ==================== 密码登录 ====================

    @Override
    public Result<LoginResponse> loginByPassword(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String password = loginDTO.getPassword();
        String clientIp = UserContext.getClientIp();

        // 1. 检查是否被锁定
        String lockKey = getLoginLockKey(clientIp, phone);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            return Result.error("登录失败次数过多，请等待 " + ttl + " 秒后重试");
        }

        // 2. 校验密码
        if (StrUtil.isBlank(password)) {
            recordLoginFail(clientIp, phone);
            return Result.error("手机号或密码错误");
        }

        // 3. 查询用户并验证密码（统一返回模糊信息，防止账号枚举）
        User user = userMapper.findByPhone(phone);
        if (user == null || !PasswordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败，手机号: {}, IP: {}, 原因: {}",
                    phone, clientIp,
                    user == null ? "用户不存在" : "密码错误");
            recordLoginFail(clientIp, phone);
            return Result.error("手机号或密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号异常，请联系客服");
        }

        // 5. 登录成功，清除失败记录
        clearLoginFail(clientIp, phone);

        // 6. 更新登录信息
        user.setLastLoginTime(new Date());
        if (StrUtil.isNotBlank(clientIp)) {
            user.setLastLoginIp(clientIp);
        }
        updateById(user);

        return buildLoginResponse(user, false);
    }

    // ==================== 验证码登录 ====================

    @Override
    public Result<LoginResponse> loginBySmsCode(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String inputCode = loginDTO.getSmsCode();
        String clientIp = UserContext.getClientIp();

        // 1. 检查是否被锁定
        String lockKey = getLoginLockKey(clientIp, phone);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            return Result.error("登录失败次数过多，请等待 " + ttl + " 秒后重试");
        }

        // 2. 校验验证码
        if (StrUtil.isBlank(inputCode)) {
            recordLoginFail(clientIp, phone);
            return Result.error("手机号或验证码错误");
        }

        // 3. 校验验证码（校验通过后自动删除）
        if (!smsCodeService.verifyAndDeleteSmsCode(phone, inputCode)) {
            recordLoginFail(clientIp, phone);
            return Result.error("手机号或验证码错误");
        }

        // 4. 查询用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            recordLoginFail(clientIp, phone);
            return Result.error("手机号或验证码错误");
        }
        if (user.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号异常，请联系客服");
        }

        // 5. 登录成功，清除失败记录
        clearLoginFail(clientIp, phone);

        // 6. 更新登录信息
        user.setLastLoginTime(new Date());
        if (StrUtil.isNotBlank(clientIp)) {
            user.setLastLoginIp(clientIp);
        }
        updateById(user);

        return buildLoginResponse(user, false);
    }

    // ==================== 根据ID登录（注册后自动登录） ====================

    @Override
    public Result<LoginResponse> loginById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号异常，请联系客服");
        }

        user.setLastLoginTime(new Date());
        String clientIp = UserContext.getClientIp();
        if (StrUtil.isNotBlank(clientIp)) {
            user.setLastLoginIp(clientIp);
        }
        updateById(user);

        return buildLoginResponse(user, true);
    }

    // ==================== 退出登录 ====================

    @Override
    public Result<Void> logout(String token) {

        if (StrUtil.isBlank(token)) {
            log.info("退出登录：Token 为空，用户可能已退出");
            UserContext.clear();
            return Result.success("退出登录成功", null);
        }

        String key = getUserTokenKey(token);
        try {
            String userJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(userJson)) {
                Long userId = JSONUtil.parseObj(userJson).getLong("id");
                String deviceListKey = getUserDeviceListKey(userId);
                stringRedisTemplate.opsForSet().remove(deviceListKey, token);
                stringRedisTemplate.delete(key);
                log.info("用户 [{}] 退出登录成功", userId);
            } else {
                log.warn("Token 已过期，Key: {}", key);
            }
        } catch (Exception e) {
            log.error("退出登录时发生错误: {}", e.getMessage());
        }

        UserContext.clear();
        return Result.success("退出登录成功", null);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建登录响应
     */
    private Result<LoginResponse> buildLoginResponse(User user, boolean isNewUser) {
        String token = UUID.randomUUID().toString();
        Long userId = user.getId();

        // 管理设备列表（限制最多 3 台设备）
        manageDeviceList(userId, token);

        // 存储用户信息到 Redis
        String key = getUserTokenKey(token);
        String userJson = JSONUtil.toJsonStr(user);
        stringRedisTemplate.opsForValue().set(
                key,
                userJson,
                TOKEN_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        LoginResponse response = new LoginResponse();
        response.setUser(userVO);
        response.setToken(token);
        response.setExpiresIn((long) TOKEN_EXPIRE_SECONDS);
        response.setIsNewUser(isNewUser);

        String message = isNewUser ? "注册成功" : "登录成功";
        log.info("用户 {} {}，设备数: {}",
                user.getPhone(), message,
                stringRedisTemplate.opsForSet().size(getUserDeviceListKey(userId)));
        return Result.success(message, response);
    }

    /**
     * 管理设备列表（限制最多 3 台设备）
     */
    private void manageDeviceList(Long userId, String newToken) {
        String deviceListKey = getUserDeviceListKey(userId);

        Set<String> tokens = stringRedisTemplate.opsForSet().members(deviceListKey);

        if (tokens != null && tokens.size() >= MAX_DEVICE_COUNT) {
            // 踢掉最早登录的设备（Set 中第一个）
            String oldestToken = tokens.iterator().next();
            stringRedisTemplate.delete(getUserTokenKey(oldestToken));
            stringRedisTemplate.opsForSet().remove(deviceListKey, oldestToken);
            log.info("用户 {} 设备数已达上限，踢掉最早设备", userId);
        }

        stringRedisTemplate.opsForSet().add(deviceListKey, newToken);
        stringRedisTemplate.expire(deviceListKey, TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    // ============ 登录失败记录（String 方式） ============

    /**
     * 记录登录失败
     */
    private void recordLoginFail(String ip, String phone) {
        if (StrUtil.isBlank(ip) || StrUtil.isBlank(phone)) {
            return;
        }

        String failKey = getLoginFailKey(ip, phone);

        // 原子递增失败次数
        Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
        if (failCount == 1) {
            // 第一次失败，设置 5 分钟过期时间
            stringRedisTemplate.expire(failKey, LOGIN_FAIL_WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        log.warn("登录失败，IP: {}, 手机号: {}, 第 {} 次失败", ip, phone, failCount);

        // 如果失败次数 >= 10，锁定账号
        if (failCount >= LOGIN_FAIL_MAX) {
            String lockKey = getLoginLockKey(ip, phone);
            stringRedisTemplate.opsForValue().set(
                    lockKey,
                    "locked",
                    LOGIN_LOCK_SECONDS,
                    TimeUnit.SECONDS
            );
            // 删除失败记录（锁定期间不累计）
            stringRedisTemplate.delete(failKey);
            log.warn("IP {} 尝试登录手机号 {} 失败 {} 次，已锁定 5 分钟", ip, phone, failCount);
        }
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFail(String ip, String phone) {
        if (StrUtil.isBlank(ip) || StrUtil.isBlank(phone)) {
            return;
        }
        String failKey = getLoginFailKey(ip, phone);
        stringRedisTemplate.delete(failKey);
    }

    // ============ 预留扩展 ============

    private void clearUserSession(Long userId) {
        // TODO: 清除权限缓存等
    }
}