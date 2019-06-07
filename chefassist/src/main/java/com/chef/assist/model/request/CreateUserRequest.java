package com.chef.assist.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest {
    @NotNull
    @Size(min = 2)
    private String username;
    @NotNull
    @JsonProperty("role_id")
    private Long roleId;
}
