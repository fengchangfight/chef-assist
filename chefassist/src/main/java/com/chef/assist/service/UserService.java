package com.chef.assist.service;

import com.chef.assist.mapper.UserMapper;
import com.chef.assist.model.User;
import com.chef.assist.model.dto.UserVO;
import com.chef.assist.utils.MyStringUtil;
import com.chef.assist.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public String currentUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return currentUserName;
        }else{
            return null;
        }
    }

    public User findUserByUserName(String username){
        return userMapper.findByUserName(username);
    }

    public String getCurrentRoleName(){
        String username = currentUsername();

        UserVO uvo = userMapper.getUserInfo(username);

        return uvo.getRoleName();
    }

    public User getCurrentUser(){
        return userMapper.findByUserName(currentUsername());
    }


    public void newUserWithNameAndRole(String name, Long roleId) throws InvalidKeySpecException, NoSuchAlgorithmException {

        User user = userMapper.findByUserName(name);
        if(user!=null){
            return;
        }
        user = new User();
        user.setRoleId(roleId);
        user.setUsername(name);
        String salt = MyStringUtil.randomAlphaNumeric(10);
        user.setPassword(SecurityUtil.generateStorngPasswordHash("admin", salt));
        user.setSalt(salt);
        userMapper.insert(user);
    }
}
