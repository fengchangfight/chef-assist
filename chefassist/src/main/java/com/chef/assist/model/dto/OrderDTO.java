package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderDTO {
    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("order_status")
    private String orderStatus;
}
