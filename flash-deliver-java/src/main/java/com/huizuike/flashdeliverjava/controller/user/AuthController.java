package com.huizuike.flashdeliverjava.controller.user;

import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.vo.TokenResponse;
import com.huizuike.flashdeliverjava.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "Token 刷新接口")
public class AuthController {

    private final JwtUtil jwtUtil;

    /**
     * 刷新 Access Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 获取新的 Access Token")
    public Result<TokenResponse> refreshToken(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(401, "Refresh Token 不能为空");
        }

        String refreshToken = authorization.substring(7);

        // 验证 Refresh Token
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return Result.error(401, "Refresh Token 无效或已过期，请重新登录");
        }

        // 生成新的 Access Token
        String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);
        if (newAccessToken == null) {
            return Result.error(401, "刷新 Token 失败，请重新登录");
        }

        TokenResponse response = new TokenResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getAccessExpiration());
        response.setRefreshExpiresIn(jwtUtil.getRefreshExpiration());

        return Result.success(response);
    }
}