package com.chef.assist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/base-service")
public class ServiceController {

    @Autowired
    private Environment env;

    @GetMapping
    public String getBaseServiceUrl(){
        return env.getProperty("base.service.url");
    }
}
