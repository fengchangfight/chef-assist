package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DishProducer {
    private Long id;

    @JsonProperty("producer_number")
    @NotNull
    private String producerNumber;
    private String description;

    // online blocking offline
    private String status;
}
