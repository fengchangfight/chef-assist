package com.chef.assist.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DishTag {
    private Long id;

    @NotNull
    private String name;
}
