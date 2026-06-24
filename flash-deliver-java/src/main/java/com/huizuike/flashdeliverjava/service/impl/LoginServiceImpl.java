package com.huizuike.flashdeliverjava.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huizuike.flashdeliverjava.common.constant.UserConstants;
import com.huizuike.flashdeliverjava.mapper.UserMapper;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.LoginDTO;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import com.huizuike.flashdeliverjava.pojo.vo.UserVO;
import com.huizuike.flashdeliverjava.service.user.LoginService;
import com.huizuike.flashdeliverjava.utils.JwtUtil;
import com.huizuike.flashdeliverjava.utils.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.baomidou.mybatisplus.extension.toolkit.Db.getById;


@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService {

    private final UserMapper userMapper;
    private final SmsCodeService smsCodeService;
    private final JwtUtil jwtUtil;

    @Override
    public Result<LoginResponse> loginByPassword(LoginDTO loginDTO) {
        String password = loginDTO.getPassword();
        String phone = loginDTO.getPhone();

//        验证密码是否为空
        if (StrUtil.isBlank(password)) {
            return Result.error("密码不能为空");
        }

//        验证用户是否存在
        User existingUser = userMapper.findByPhone(phone);
        if (existingUser==null) {
            return Result.error("用户不存在，请先注册");
        }
        if (existingUser.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号已被禁用，请联系客服");
        }
        if (!PasswordEncoder.matches(password, existingUser.getPassword())) {
            return Result.error("密码错误");
        }
        // 更新最后登录时间
        existingUser.setLastLoginTime(new java.util.Date());
        updateById(existingUser);

        // 生成 Token 并返回
        return buildLoginResponse(existingUser, false);
    }

    @Override
    public Result<LoginResponse> loginBySmsCode(LoginDTO loginDTO) {
        String smsCode = loginDTO.getSmsCode();
        String phone = loginDTO.getPhone();

//        验证验证码
        if (StrUtil.isBlank(smsCode)) {
            return Result.error("验证码不能为空");
        }
        if (!smsCodeService.verifyAndDeleteSmsCode(phone,smsCode)) {
            return Result.error("验证码错误或已过期");
        }

//        验证用户
        User existingUser = userMapper.findByPhone(phone);
        if (existingUser==null) {
            return Result.error("用户不存在，请先注册");
        }

        if (existingUser.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号已被禁用，请联系客服");
        }

        // 更新最后登录时间
        existingUser.setLastLoginTime(new java.util.Date());
        updateById(existingUser);

        // 生成 Token 并返回
        return buildLoginResponse(existingUser, false);
    }

    @Override
    public Result<LoginResponse> loginById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getStatus() == UserConstants.STATUS_DISABLED) {
            return Result.error("账号已被禁用，请联系客服");
        }
        return buildLoginResponse(user, true);
    }

    /**
     * 构建登录响应（包含 JWT Token）
     */
    private Result<LoginResponse> buildLoginResponse(User user, boolean isNewUser) {
        // 生成 Access Token 和 Refresh Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        LoginResponse response = new LoginResponse();
        response.setUser(userVO);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getAccessExpiration());
        response.setRefreshExpiresIn(jwtUtil.getRefreshExpiration());
        response.setIsNewUser(isNewUser);

        String message = isNewUser ? "注册成功" : "登录成功";
        return Result.success(message, response);
    }
}
