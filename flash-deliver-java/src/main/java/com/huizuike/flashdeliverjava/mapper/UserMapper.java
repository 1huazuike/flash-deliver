package com.huizuike.flashdeliverjava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE phone = #{phone} AND is_deleted = 0")
    User findByPhone(String phone);
}
