package com.huizuike.flashdeliverjava.common.constant;


import java.util.Random;

/**
 * 默认头像常量类
 *
 * @author huazuike
 * @date 2026-06-12
 */
public class DefaultAvatarConstants {

    /**
     * 默认头像基础路径
     */
    public static final String AVATAR_BASE_PATH = "avatar/";

    /**
     * 默认头像文件名列表
     */
    public static final String[] DEFAULT_AVATARS = {
            "avatar1.png",
            "avatar2.png",
            "avatar3.png",
            "avatar4.png",
            "avatar5.png"
    };

    private static final Random RANDOM = new Random();

    /**
     * 获取随机默认头像（带路径前缀）
     *
     * @return 完整头像路径，如 "avatar/avatar3.png"
     */
    public static String getRandomAvatar() {
        int index = RANDOM.nextInt(DEFAULT_AVATARS.length);
        return AVATAR_BASE_PATH + DEFAULT_AVATARS[index];
    }

}
