package com.huizuike.flashdeliverjava.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "注册请求")
public class LoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18812345678")
    private String phone;

    @Length(min = 6, max = 20, message = "密码长度6-20位")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确（6位数字）")
    @Schema(description = "验证码", example = "123456")
    private String smsCode;
}