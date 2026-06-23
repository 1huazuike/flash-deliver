package com.huizuike.flashdeliverjava.controller.user;


import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.LoginDTO;
import com.huizuike.flashdeliverjava.pojo.dto.RegisterDTO;
import com.huizuike.flashdeliverjava.pojo.dto.SmsCodeDTO;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import com.huizuike.flashdeliverjava.service.impl.SmsCodeService;
import com.huizuike.flashdeliverjava.service.user.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理接口")
public class UserController {

    private final RegisterService registerService;
    private final SmsCodeService smsCodeService;

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
    @Operation(summary = "用户登录")
    public Result<LoginResponse> Login(@Valid @RequestBody LoginDTO loginDTO){
        // TODO: 实现登录逻辑（密码登录 + 验证码登录）
        return Result.success(null);
    }
}
