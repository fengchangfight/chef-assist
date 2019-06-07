package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DishProducerBoardDTO {
    private Long id;

    @JsonProperty("producer_number")
    @NotNull
    private String producerNumber;
    private String description;

    // online blocking offline
    private String status;

    @Data
    public static class BoardInnerItems{
        @JsonProperty("item_id")
        private Long itemId;

        @JsonProperty("dish_name")
        private String dishName;

        @JsonProperty("dish_count")
        private Integer dishCount;

        private String description;

        public BoardInnerItems(Long itemId, String dishName, Integer dishCount, String description) {
            this.itemId = itemId;
            this.dishName = dishName;
            this.dishCount = dishCount;
            this.description = description;
        }
    }

    @JsonProperty("inner_items")
    private List<BoardInnerItems> innerItems;
}
