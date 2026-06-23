package com.huizuike.flashdeliverjava.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "注册请求")
public class RegisterDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18812345678")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度6-20位")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确（6位数字）")
    @Schema(description = "验证码", example = "123456")
    private String smsCode;

    @Schema(description = "昵称（可选，默认'用户+随机数字'）", example = "吃货小明")
    private String nickname;

    @Schema(description = "性别（可选，默认0未知）", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Schema(description = "头像（可选，默认随机默认头像）", example = "https://xxx.com/default-avatar-1.jpg")
    private String avatar;

    /**
     * 角色（固定为 CUSTOMER，不允许前端传值）
     * 注意：不使用 @Schema，前端不可见
     */
    private String role = "CUSTOMER";

    /**
     * 状态（固定为 1 正常）
     */
    private Integer status = 1;
}