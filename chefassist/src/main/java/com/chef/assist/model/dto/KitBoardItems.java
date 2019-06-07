package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KitBoardItems {
    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("producer_id")
    private Long producerId;

    @JsonProperty("dish_name")
    private String dishName;

    @JsonProperty("dish_count")
    private Integer dishCount;

    private String description;
}
