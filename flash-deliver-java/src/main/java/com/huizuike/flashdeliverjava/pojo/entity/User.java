package com.huizuike.flashdeliverjava.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * 用户表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户实体")
public class User {

    /**
     * 用户ID（使用全局唯一id生成器进行生成）
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "18812345678")
    private String phone;

    /**
     * 密码（加密存储）
     */
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度6-20位")
    @Schema(description = "密码（加密）", example = "123456")
    @JsonIgnore  // 返回时隐藏密码字段
    private String password;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "吃货小明")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://xxx.com/avatar.jpg")
    private String avatar;

    /**
     * 性别 0未知 1男 2女
     */
    @Schema(description = "性别", allowableValues = {"0", "1", "2"})
    private Integer gender;

    /**
     * 角色: CUSTOMER/MERCHANT/RIDER/ADMIN
     */
    @Schema(description = "角色", allowableValues = {"CUSTOMER", "MERCHANT", "RIDER", "ADMIN"})
    private String role;

    /**
     * 状态 0禁用 1正常
     */
    @Schema(description = "状态", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP", example = "192.168.1.1")
    private String lastLoginIp;

    /**
     * 创建时间（自动生成）
     */
    @Schema(description = "创建时间")
    private Date createdAt;

    /**
     * 更新时间（自动生成）
     */
    @Schema(description = "更新时间")
    private Date updatedAt;

    /**
     * 逻辑删除 0正常 1删除
     */
    @Schema(description = "逻辑删除", hidden = true)
    private Integer isDeleted;
}