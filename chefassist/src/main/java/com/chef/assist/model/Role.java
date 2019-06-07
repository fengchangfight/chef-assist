package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Role {
    private Long id;

    @JsonProperty("role_name")
    private String roleName;
}
