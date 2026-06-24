package com.huizuike.flashdeliverjava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huizuike.flashdeliverjava.pojo.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号查询用户（逻辑删除过滤）
     */
    @Select("SELECT * FROM `user` WHERE phone = #{phone} AND is_deleted = 0")
    User findByPhone(@Param("phone") String phone);
}
