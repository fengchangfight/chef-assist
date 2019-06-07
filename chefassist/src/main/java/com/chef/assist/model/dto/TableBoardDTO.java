package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TableBoardDTO {
    private Long id;

    @JsonProperty("table_number")
    @NotNull
    private String tableNumber;
    private String description;

    @Data
    public static class BoardInnerItems{
        @JsonProperty("item_id")
        private Long itemId;

        @JsonProperty("dish_name")
        private String dishName;

        @JsonProperty("dish_count")
        private Integer dishCount;

        private String description;

        private String status;

        public BoardInnerItems(Long itemId, String dishName, Integer dishCount, String description, String status) {
            this.itemId = itemId;
            this.dishName = dishName;
            this.dishCount = dishCount;
            this.description = description;
            this.status = status;
        }
    }

    @JsonProperty("inner_items")
    private List<BoardInnerItems> innerItems;
}
