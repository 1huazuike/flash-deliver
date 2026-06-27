package com.huizuike.flashdeliverjava.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "修改密码请求")
public class UpdatePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @Length(min = 6, max = 20, message = "旧密码长度6-20位")
    @Schema(description = "旧密码", example = "123456")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Length(min = 6, max = 20, message = "新密码长度6-20位")
    @Schema(description = "新密码", example = "654321")
    private String newPassword;
}