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
public class ChangepassRequest {
    @JsonProperty("new_password")
    @NotNull
    @Size(min=4, max=20)
    private String newPassword;

    @NotNull
    private String password;
}
