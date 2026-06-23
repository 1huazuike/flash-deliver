package com.huizuike.flashdeliverjava.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "发送验证码请求")
public class SmsCodeDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18812345678", required = true)
    private String phone;

    @NotBlank(message = "验证码类型不能为空")
    @Schema(description = "验证码类型", example = "login", allowableValues = {"login", "register", "reset"})
    private String type;
}