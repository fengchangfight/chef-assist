package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EditTableRequest {
    @JsonProperty("table_number")
    @NotNull
    private String tableNumber;
    private String description;
}
