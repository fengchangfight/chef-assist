package com.chef.assist.config;


import com.chef.assist.mapper.UserMapper;
import com.chef.assist.model.User;
import com.chef.assist.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserMapper userMapper;

    private boolean correctUserNameAndPassword(String username, String password){
        User user = userMapper.findByUserName(username);

        String passwordInDB = user.getPassword();
        String encryptedPassword=null;
        try {
            encryptedPassword = SecurityUtil.generateStorngPasswordHash(password, user.getSalt());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }

        if(encryptedPassword.equals(passwordInDB)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        if(correctUserNameAndPassword(name, password)){
            User user = userMapper.findByUserName(name);
            return new UsernamePasswordAuthenticationToken(user.getUsername(), password, new ArrayList<>());
        }else{
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}