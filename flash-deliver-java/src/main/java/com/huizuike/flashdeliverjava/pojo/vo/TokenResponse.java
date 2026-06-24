package com.huizuike.flashdeliverjava.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Token 响应")
public class TokenResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "Access Token 过期时间（秒）")
    private Long expiresIn;

    @Schema(description = "Refresh Token 过期时间（秒）")
    private Long refreshExpiresIn;
}