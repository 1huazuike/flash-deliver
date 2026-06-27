package com.huizuike.flashdeliverjava.service.user;


import com.baomidou.mybatisplus.extension.service.IService;
import com.huizuike.flashdeliverjava.pojo.common.Result;
import com.huizuike.flashdeliverjava.pojo.dto.RegisterDTO;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import com.huizuike.flashdeliverjava.pojo.vo.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

public interface RegisterService extends IService<User> {
    Result<LoginResponse> register(@Valid RegisterDTO registerDTO);
}
