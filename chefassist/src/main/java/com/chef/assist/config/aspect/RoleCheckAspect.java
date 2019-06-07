package com.chef.assist.config.aspect;

import com.chef.assist.config.annotation.RoleCheck;
import com.chef.assist.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {
    @Autowired
    private UserService userService;

    @Before("@annotation(com.chef.assist.config.annotation.RoleCheck)")
    public void logExecutionTime(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RoleCheck annotation = method.getAnnotation(RoleCheck.class);

        String currentRoleName = userService.getCurrentRoleName();

        if(!Arrays.asList(annotation.roles()).contains(currentRoleName)){
            throw new RuntimeException("您的角色没有权限进行此操作");
        }
    }
}
