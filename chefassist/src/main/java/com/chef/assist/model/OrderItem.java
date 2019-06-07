package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class OrderItem {
    private Long id;

    @JsonProperty("order_id")
    @NotNull
    private Long orderId;

    @NotNull
    @JsonProperty("table_id")
    private Long tableId;

    @NotNull
    @JsonProperty("dish_id")
    private Long dishId;

    @NotNull
    @JsonProperty("dish_count")
    private Integer dishCount;

    @JsonProperty("assign_to")
    private Long assignTo;

    private String status;

    @JsonProperty("last_update_time")
    private Date lastUpdateTime;

    private String description;
    
}
