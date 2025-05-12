package com.zwj.backend.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zwj.backend.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectOneByUsername(String username);
}