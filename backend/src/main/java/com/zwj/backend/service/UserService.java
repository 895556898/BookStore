package com.zwj.backend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.User;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.zwj.backend.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
//    UserMapper userMapper = MybatisFlexBootstrap.getInstance().getMapper(UserMapper.class);

    @Resource
    private UserMapper userMapper;

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
        user.setRole("user");
        userMapper.insert(user);
        return StatusCode.REGISTER_SUCCESS;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
        if (user == null) {
            throw new UsernameNotFoundException("未找到用户");
        }
        String pwd = user.getPassword();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> "ROLE_" + user.getRole());
        System.out.println("loadUserByUsername: "+user.getUsername());
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                                                                        pwd,
                                                                        true,
                                                                        true,
                                                                        true,
                                                                        true,
                                                                        authorities);

    }


    public StatusCode getUserByUsername(String username) {
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username",username));
        if (user == null) {
            return StatusCode.USER_NOT_EXIST;
        }
        return StatusCode.LOGIN_SUCCESS;
    }
}
