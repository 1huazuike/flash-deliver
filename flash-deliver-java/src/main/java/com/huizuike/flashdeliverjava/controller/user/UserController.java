package com.huizuike.flashdeliverjava.controller.user;


import com.huizuike.flashdeliverjava.common.constant.RedisKeyConstants;
import com.huizuike.flashdeliverjava.common.context.UserContext;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.*;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import com.huizuike.flashdeliverjava.pojo.vo.UserVO;
import com.huizuike.flashdeliverjava.service.impl.SmsCodeService;
import com.huizuike.flashdeliverjava.service.user.LoginService;
import com.huizuike.flashdeliverjava.service.user.RegisterService;
import com.huizuike.flashdeliverjava.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理接口")
@Slf4j
public class UserController {

    private final RegisterService registerService;
    private final SmsCodeService smsCodeService;
    private final LoginService loginService;
    private final UserService userService;

    // ==================== 注册/登录/退出 ====================

    /**
     * 发送验证码
     */
    @PostMapping("/sms-code")
    @Operation(summary = "发送验证码")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeDTO smsCodeDTO) {
//        TODO 先模拟发送
        smsCodeService.sendSmsCode(smsCodeDTO.getPhone());
        return Result.success("验证码发送成功", null);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterDTO registerDTO) {
//        注册成功返回LoginResponse(包含用户信息和token)
        return registerService.register(registerDTO);
    }


    /**
     * 用户登录（暂未实现 JWT，返回占位）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持密码登录和验证码登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 密码登录
        if (loginDTO.getPassword() != null && !loginDTO.getPassword().isEmpty()) {
            return loginService.loginByPassword(loginDTO);
        }

        // 验证码登录
        if (loginDTO.getSmsCode() != null && !loginDTO.getSmsCode().isEmpty()) {
            return loginService.loginBySmsCode(loginDTO);
        }

        return Result.error("请使用密码或验证码登录");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("退出登录失败：Authorization 格式不正确");
            return Result.error("退出登录失败");
        }

        String token = authorization.substring(7);

        return loginService.logout(token);
    }


    // ==================== 用户信息管理 ====================

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.getUserInfo(userId);
    }

    @PutMapping("/info")
    @Operation(summary = "修改用户信息", description = "修改昵称、头像、性别")
    public Result<Void> updateUserInfo(@Valid @RequestBody UpdateUserInfoDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.updateUserInfo(userId, dto);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "需要验证旧密码")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.updatePassword(userId, dto);
    }
}
