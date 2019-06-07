package com.chef.assist.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignRoleRequest {
    @JsonProperty("user_id")
    Long userId;

    @JsonProperty("role_id")
    Long roleId;
}
