package com.zwj.backend.service;

import com.mybatisflex.core.MybatisFlexBootstrap;
import com.mybatisflex.core.query.QueryWrapper;
import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.zwj.backend.mapper.UserMapper;

@Service
public class UserService {
    UserMapper userMapper = MybatisFlexBootstrap.getInstance().getMapper(UserMapper.class);

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //注册
    public StatusCode register(String username, String password, String phone) {
        if(userMapper.selectCountByQuery(new QueryWrapper().eq("username", username)) != 0){
            return StatusCode.REGISTER_USERNAME_EXIST;
        }

        if(userMapper.selectCountByQuery(new QueryWrapper().eq("phone", phone)) != 0){
            return StatusCode.REGISTER_PHONE_EXIST;
        }

        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptedPassword);
        user.setPhone(phone);
        userMapper.insert(user);
        return StatusCode.REGISTER_SUCCESS;
    }

    //登录
    public StatusCode login(String username, String password) {
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
        if(user == null) {
            return StatusCode.LOGIN_FAIL;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return StatusCode.LOGIN_SUCCESS;
        } else {
            return StatusCode.LOGIN_FAIL;
        }
    }

    public StatusCode findUser(String username) {
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
    }

    public CurrentUserVO getCurrentUser(String username) {
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
        return new CurrentUserVO(user.getUsername(), user.getAvatar(), user.getId(), user.getEmail());
    }
}
