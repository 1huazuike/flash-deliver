package com.huizuike.flashdeliverjava.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import com.huizuike.flashdeliverjava.common.constant.UserConstants;
import com.huizuike.flashdeliverjava.mapper.UserMapper;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.UpdatePasswordDTO;
import com.huizuike.flashdeliverjava.pojo.dto.UpdateUserInfoDTO;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.pojo.vo.UserVO;
import com.huizuike.flashdeliverjava.service.user.UserService;
import com.huizuike.flashdeliverjava.utils.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;

    // ==================== 获取用户信息 ====================

    @Override
    public Result<UserVO> getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    // ==================== 修改用户信息 ====================

    @Override
    @Transactional
    public Result<Void> updateUserInfo(Long userId, UpdateUserInfoDTO dto) {
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 1. 更新字段
        boolean hasUpdate = false;
        if (StrUtil.isNotBlank(dto.getNickname())) {
            user.setNickname(dto.getNickname());
            hasUpdate = true;
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
            hasUpdate = true;
        }
        if (StrUtil.isNotBlank(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            return Result.error("没有需要更新的信息");
        }

        // 2. 保存到数据库
        updateById(user);

        // 3. ✅ 更新该用户所有设备的 Redis 缓存
        updateAllDeviceCache(user);

        log.info("用户 {} 信息更新成功", userId);
        return Result.success();
    }

    // ==================== 修改密码 ====================

    @Override
    @Transactional
    public Result<Void> updatePassword(Long userId, UpdatePasswordDTO dto) {
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 1. 校验旧密码
        if (!PasswordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            return Result.error("新密码不能与旧密码相同");
        }

        // 2. 加密新密码并更新
        user.setPassword(PasswordEncoder.encode(dto.getNewPassword()));
        updateById(user);

        // 3. ✅ 清除该用户所有设备缓存（强制重新登录）
        clearAllDeviceCache(userId);

        log.info("用户 {} 密码修改成功，已强制所有设备退出", userId);
        return Result.success("密码修改成功，请重新登录",null);
    }

    // ==================== 私有方法 ====================

    /**
     * 更新该用户所有设备的 Redis 缓存
     */
    private void updateAllDeviceCache(User user) {
        String deviceListKey = getUserDeviceListKey(user.getId());
        Set<String> tokens = stringRedisTemplate.opsForSet().members(deviceListKey);

        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        String userJson = JSONUtil.toJsonStr(user);
        int updatedCount = 0;

        for (String token : tokens) {
            String tokenKey = getUserTokenKey(token);
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenKey))) {
                stringRedisTemplate.opsForValue().set(
                        tokenKey,
                        userJson,
                        TOKEN_EXPIRE_SECONDS,
                        TimeUnit.SECONDS
                );
                updatedCount++;
            }
        }

        log.debug("用户 {} 信息已同步到 {} 个设备缓存", user.getId(), updatedCount);
    }

    /**
     * 清除该用户所有设备缓存
     */
    private void clearAllDeviceCache(Long userId) {
        String deviceListKey = getUserDeviceListKey(userId);
        Set<String> tokens = stringRedisTemplate.opsForSet().members(deviceListKey);

        // 1. 删除所有 Token 缓存
        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                stringRedisTemplate.delete(getUserTokenKey(token));
            }
        }

        // 2. 删除设备列表
        stringRedisTemplate.delete(deviceListKey);
    }
}