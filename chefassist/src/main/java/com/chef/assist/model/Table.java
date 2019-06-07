package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Table {
    private Long id;

    @JsonProperty("table_number")
    @NotNull
    private String tableNumber;

    private String description;
}
