package com.zwj.backend.service;

import com.mybatisflex.core.MybatisFlexBootstrap;
import com.mybatisflex.core.query.QueryWrapper;
import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.User;
import org.springframework.stereotype.Service;
import com.zwj.backend.mapper.UserMapper;

@Service
public class UserService {
    UserMapper userMapper = MybatisFlexBootstrap.getInstance().getMapper(UserMapper.class);

    //注册
    public StatusCode register(String username, String password, String phone) {
        if(userMapper.selectCountByQuery(new QueryWrapper().eq("username", username)) != 0){
            return StatusCode.REGISTER_USERNAME_DUPLICATE;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        userMapper.insert(user);
        return StatusCode.REGISTER_SUCCESS;
    }
}
