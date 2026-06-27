package com.huizuike.flashdeliverjava.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.UpdatePasswordDTO;
import com.huizuike.flashdeliverjava.pojo.dto.UpdateUserInfoDTO;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.pojo.vo.UserVO;

public interface UserService extends IService<User> {

    /**
     * 获取用户信息
     */
    Result<UserVO> getUserInfo(Long userId);

    /**
     * 修改用户信息
     */
    Result<Void> updateUserInfo(Long userId, UpdateUserInfoDTO dto);

    /**
     * 修改密码
     */
    Result<Void> updatePassword(Long userId, UpdatePasswordDTO dto);

    /**
     * 根据手机号查询用户（在后面进行微服务拆解可能会用到）
     */
//    TODO 在后面进行微服务拆解可能会用到
//    User findByPhone(String phone);
}