package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String salt;
    private String password;

    @JsonProperty("role_id")
    private Long roleId;
}
