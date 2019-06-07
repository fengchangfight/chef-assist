package com.chef.assist.controller;

import com.chef.assist.mapper.RoleMapper;
import com.chef.assist.model.Role;
import com.chef.assist.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/role")
@DependsOn("liquibase")
public class RoleController {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Role> getAllRoles(){
        return roleMapper.findAllRoles();
    }

    @PostConstruct
    public void initRolesAndAdmin() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Role roleAdmin = roleMapper.findByRoleName("管理员");
        Long adminId = null;
        if(roleAdmin==null){
            roleAdmin = new Role();
            roleAdmin.setRoleName("管理员");
            adminId = roleMapper.insert(roleAdmin);
        }

        Role roleChef = roleMapper.findByRoleName("厨师");
        if(roleChef==null){
            roleChef = new Role();
            roleChef.setRoleName("厨师");
            roleMapper.insert(roleChef);
        }

        Role roleWaiter = roleMapper.findByRoleName("服务员");
        if(roleWaiter==null){
            roleWaiter = new Role();
            roleWaiter.setRoleName("服务员");
            roleMapper.insert(roleWaiter);
        }

        userService.newUserWithNameAndRole("admin", adminId);
    }
}
