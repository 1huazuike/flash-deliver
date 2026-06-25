package com.huizuike.flashdeliverjava.service.user;

import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.LoginDTO;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;

public interface LoginService {

    /**
     * 密码登录
     */
    Result<LoginResponse> loginByPassword(LoginDTO loginDTO);

    /**
     * 验证码登录
     */
    Result<LoginResponse> loginBySmsCode(LoginDTO loginDTO);

    /**
     * 根据用户ID登录（用于注册后自动登录）
     */
    Result<LoginResponse> loginById(Long userId);

    /**
     * 退出登录
     */
    Result<Void> logout(String authorization);

}