package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class Dish {
    private Long id;
    @NotNull
    private String name;
    private String description;
    private String tags;

    @JsonProperty("expected_cooking_time")
    private Integer expectedCookingTime;

    private String thumbnail;

    private Double price=0.0;
}
