package com.zwj.backend.service;

import com.mybatisflex.core.MybatisFlexBootstrap;
import com.mybatisflex.core.query.QueryWrapper;
import com.zwj.backend.common.StatusCode;
import com.zwj.backend.entity.User;
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
    UserMapper userMapper = MybatisFlexBootstrap.getInstance().getMapper(UserMapper.class);

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private DefaultErrorAttributes defaultErrorAttributes;


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

    //登录(先判断用户名再判断手机号)
    public StatusCode Login(String textA, String password) {
        String username = textA;
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
        if(user == null) {
            String phone = textA;
            user = userMapper.selectOneByQuery(new QueryWrapper().eq("phone", phone));
            if(user == null) {
                return StatusCode.LOGIN_FAIL;
            }
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return StatusCode.LOGIN_SUCCESS;
        } else {
            return StatusCode.LOGIN_FAIL;
        }
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

//    public StatusCode findUser(String username) {
//        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
//    }
//
//    public CurrentUserVO getCurrentUser(String username) {
//        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username", username));
//        return new CurrentUserVO(user.getUsername(), user.getAvatar(), user.getId(), user.getEmail());
//    }

    public StatusCode getUserByUsername(String username) {
        User user = userMapper.selectOneByQuery(new QueryWrapper().eq("username",username));
        if (user == null) {
            return StatusCode.USER_NOT_EXIST;
        }
        return StatusCode.LOGIN_SUCCESS;
    }
}
