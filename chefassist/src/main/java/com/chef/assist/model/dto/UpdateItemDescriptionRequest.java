package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateItemDescriptionRequest {
    @JsonProperty("item_id")
    private Long itemId;

    private String description;
}
