package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TableBoardItems {
    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("table_id")
    private Long tableId;

    @JsonProperty("dish_name")
    private String dishName;

    @JsonProperty("dish_count")
    private Integer dishCount;

    private String description;

    private String status;
}
