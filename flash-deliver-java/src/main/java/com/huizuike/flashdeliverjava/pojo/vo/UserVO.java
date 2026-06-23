package com.huizuike.flashdeliverjava.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户信息响应")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "最后登录时间")
    private Date lastLoginTime;

    @Schema(description = "注册时间")
    private Date createdAt;
}