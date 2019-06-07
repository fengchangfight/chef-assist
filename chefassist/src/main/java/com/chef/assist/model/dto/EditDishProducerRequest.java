package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EditDishProducerRequest {
    @JsonProperty("producer_number")
    @NotNull
    private String producerNumber;
    private String description;
}
