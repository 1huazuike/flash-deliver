package com.huizuike.flashdeliverjava.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "登录/注册响应")
public class LoginResponse {

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "Token过期时间（毫秒）", example = "86400000")
    private Long expiresIn;

    @Schema(description = "是否新注册用户", example = "true")
    private Boolean isNewUser;
}