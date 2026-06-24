package com.huizuike.flashdeliverjava.service.user;


import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.LoginDTO;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface LoginService {

    /**
     * 密码登录
     * @param loginDTO
     * @return
     */
    Result<LoginResponse> loginByPassword(@Valid LoginDTO loginDTO);

    /**
     * 验证码登录
     * @param loginDTO
     * @return
     */
    Result<LoginResponse> loginBySmsCode(@Valid LoginDTO loginDTO);

    /**
     * 根据用户ID登录（用于注册后自动登录）
     */
    Result<LoginResponse> loginById(Long userId);
}
