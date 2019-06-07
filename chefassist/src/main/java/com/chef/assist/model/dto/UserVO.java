package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserVO {
    @JsonProperty("user_id")
    private Long userId;
    private String username;

    @JsonProperty("role_name")
    private String roleName;

    public UserVO(Long id, String username, String roleName) {
        this.userId = id;
        this.username = username;
        this.roleName = roleName;
    }
}
