package com.huizuike.flashdeliverjava.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改用户信息请求")
public class UpdateUserInfoDTO {

    @Size(max = 50, message = "昵称长度不能超过50")
    @Schema(description = "昵称", example = "吃货小明")
    private String nickname;

    @Schema(description = "性别", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Size(max = 255, message = "头像URL长度不能超过255")
    @Schema(description = "头像URL", example = "/avatar/avatar1.png")
    private String avatar;
}