package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UpdateCountRequest {
    @NotNull
    @JsonProperty("item_id")
    private Long itemId;
    @NotNull
    @Min(0)
    private Integer count;
}
