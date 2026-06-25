package com.huizuike.flashdeliverjava.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huizuike.flashdeliverjava.common.constant.DefaultAvatarConstants;
import com.huizuike.flashdeliverjava.common.constant.UserConstants;
import com.huizuike.flashdeliverjava.mapper.UserMapper;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.RegisterDTO;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import com.huizuike.flashdeliverjava.pojo.vo.UserVO;
import com.huizuike.flashdeliverjava.service.user.LoginService;
import com.huizuike.flashdeliverjava.service.user.RegisterService;
import com.huizuike.flashdeliverjava.utils.PasswordEncoder;
import com.huizuike.flashdeliverjava.utils.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.huizuike.flashdeliverjava.common.constant.UserConstants.IS_DELETED_NO;
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl extends ServiceImpl<UserMapper, User> implements RegisterService {

    private final SmsCodeService smsCodeService;
    private final SnowflakeIdGenerator idGenerator;
    private final LoginService loginService;

    /**
     * 用户注册
     * @param registerDTO
     * @return LoginResponse
     */
    @Override
    @Transactional
    public Result<LoginResponse> register(RegisterDTO registerDTO) {

        String phone = registerDTO.getPhone();
        String smsCode = registerDTO.getSmsCode();

//        校验验证码
        if (!smsCodeService.verifyAndDeleteSmsCode(phone, smsCode)) {
            return Result.error("验证码错误或已过期");
        }
//        检查手机是否注册
        User existingUser = findByPhone(phone);
        if (existingUser!=null)
            return loginService.loginById(existingUser.getId());
//        创建新用户
        User user = new User();
        user.setId(idGenerator.nextId());
        user.setPhone(phone);
        user.setPassword(PasswordEncoder.encode(registerDTO.getPassword()));

        //        昵称
        user.setNickname(StrUtil.isNotBlank(registerDTO.getNickname())
                ?registerDTO.getNickname()
                :idGenerator.generateDefaultNickname());

        //        头像
        user.setAvatar(StrUtil.isNotBlank(registerDTO.getAvatar())
                ? registerDTO.getAvatar()
                : DefaultAvatarConstants.getRandomAvatar());

        // 性别
        user.setGender(registerDTO.getGender() != null
                ? registerDTO.getGender()
                : UserConstants.GENDER_UNKNOWN);

        user.setRole(UserConstants.ROLE_CUSTOMER);
        user.setStatus(UserConstants.STATUS_NORMAL);

        // 4. 保存用户
        save(user);

        // 5. 调用登录服务，自动登录
        return loginService.loginById(user.getId());
    }

    /**
     * 根据用户手机号查询用户
     * @param phone
     * @return
     */
    private User findByPhone(String phone){
        return lambdaQuery()
                .eq(User::getPhone,phone)
                .eq(User::getIsDeleted,IS_DELETED_NO)
                .one();
    }
}
