package com.chef.assist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class Order {
    private Long id;
    @JsonProperty("order_number")
    @NotNull
    private String orderNumber;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("start_time")
    private Date startTime;

    @JsonProperty("created_by")
    private Long createdBy;

    @JsonProperty("end_time")
    private Date endTime;
}
