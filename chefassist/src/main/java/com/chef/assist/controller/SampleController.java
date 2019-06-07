package com.chef.assist.controller;

import com.chef.assist.mapper.RoleMapper;
import com.chef.assist.mapper.UserMapper;
import com.chef.assist.model.Role;
import com.chef.assist.model.User;
import com.chef.assist.utils.MyStringUtil;
import com.chef.assist.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/sample")
public class SampleController {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserMapper userMapper;

//    @PostMapping("/fake-reg")
//    public void fakeReg() throws InvalidKeySpecException, NoSuchAlgorithmException {
//        User user = new User();
//        Role role = roleMapper.findByRoleName("管理员");
//        user.setRoleId(role.getId());
//        user.setUsername("admin");
//        String salt= MyStringUtil.randomAlphaNumeric(10);;
//        user.setSalt(salt);
//        user.setPassword(SecurityUtil.generateStorngPasswordHash("admin", salt));
//        userMapper.insert(user);
//    }
}
