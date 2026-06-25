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

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Date;
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

        // 1. 校验密码
        if (StrUtil.isBlank(password)) {
            return Result.error("密码不能为空");
        }

        // 2. 查询用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            return Result.error("用户不存在，请先注册");
        }
        if (user.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号已被禁用，请联系客服");
        }
        if (!PasswordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        //  从 UserContext 获取 IP（拦截器已经注入）
        String clientIp = UserContext.getClientIp();

        // 3. 更新最后登录时间
        user.setLastLoginTime(new Date());
        if (StrUtil.isNotBlank(clientIp)) {
            user.setLastLoginIp(clientIp);
        }

        updateById(user);

        // 4. 构建响应
        return buildLoginResponse(user, false);
    }

    // ==================== 验证码登录 ====================

    @Override
    public Result<LoginResponse> loginBySmsCode(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String inputCode = loginDTO.getSmsCode();

        // 1. 校验验证码
        if (StrUtil.isBlank(inputCode)) {
            return Result.error("验证码不能为空");
        }

        // 2. 校验验证码（校验通过后自动删除）
        if (!smsCodeService.verifyAndDeleteSmsCode(phone, inputCode)) {
            return Result.error("验证码错误或已过期");
        }

        // 3. 查询用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            return Result.error("用户不存在，请先注册");
        }
        if (user.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号已被禁用，请联系客服");
        }

        // 从 UserContext 获取 IP
        String clientIp = UserContext.getClientIp();

        // 4. 更新最后登录时间
        user.setLastLoginTime(new Date());
        if (StrUtil.isNotBlank(clientIp)) {
            user.setLastLoginIp(clientIp);
        }
        updateById(user);

        // 5. 构建响应
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
            return Result.error("账号已被禁用，请联系客服");
        }

        // 更新最后登录时间
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
    public Result<Void> logout(String authorization){

        // 1. 提取 Token
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("不合规的参数");
            return Result.error("退出登录失败");
        }

        String token = authorization.substring(7);
        // 2. 如果 Token 不存在，直接返回（可能已经是退出状态）
        if (StrUtil.isBlank(token)) {
            log.info("Token 为空，直接退出");
            return Result.success("退出登录成功",null);
        }
        // 3. 从 Redis 获取用户信息
        String key = RedisKeyConstants.getUserTokenKey(token);
        try {
            // 3.1 如果 Token 存在且有效，删除并记录日志
            String userJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(userJson)) {
                Long userId = JSONUtil.parseObj(userJson).getLong("id");
                stringRedisTemplate.delete(key);

                // 3.2 清除其他会话数据（如果有）
                clearUserSession(userId);

                log.info("用户 [{}] 退出登录成功", userId);
            } else {
                // 3.3 Token 已过期，也返回成功（用户本来就是未登录状态）
                log.warn("Token 已过期，Key: {}", key);
            }
        } catch (Exception e) {
            // 3.4 如果过程中出错，记录日志但不影响退出
            log.error("退出登录时发生错误: {}", e.getMessage());
        }

        UserContext.clear();
        return Result.success("退出登录成功",null);
    }


    // ==================== 私有方法 ====================

    /**
     * 构建登录响应，存储用户信息到 Redis
     */
    private Result<LoginResponse> buildLoginResponse(User user, boolean isNewUser) {
        // 1. 生成随机 Token（UUID）
        String token = UUID.randomUUID().toString();

        // 2. 存储用户信息到 Redis（30分钟过期）
        String key = RedisKeyConstants.getUserTokenKey(token);
        String userJson = JSONUtil.toJsonStr(user);
        stringRedisTemplate.opsForValue().set(
                key,
                userJson,
                RedisKeyConstants.TOKEN_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        // 3. 构建响应
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        LoginResponse response = new LoginResponse();
        response.setUser(userVO);
        response.setToken(token);
        response.setExpiresIn((long) RedisKeyConstants.TOKEN_EXPIRE_SECONDS);
        response.setIsNewUser(isNewUser);

        String message = isNewUser ? "注册成功" : "登录成功";
        log.info("用户 {} {}，Token: {}", user.getPhone(), message, token.substring(0, 8) + "...");
        return Result.success(message, response);
    }

    /**
     * 清除用户的所有会话数据
     */
    private void clearUserSession(Long userId) {
        // TODO: 清除权限缓存、设备记录等
//        // 清除权限缓存
//        stringRedisTemplate.delete(RedisKeyConstants.getUserAuthKey(userId));
//        // 清除菜单缓存
//        stringRedisTemplate.delete(RedisKeyConstants.getUserMenuKey(userId));
//        // 如果有设备登录记录，也清除
//        stringRedisTemplate.delete(RedisKeyConstants.getUserDeviceKey(userId));
    }

}