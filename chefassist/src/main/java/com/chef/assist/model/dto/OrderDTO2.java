package com.chef.assist.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xiefengchang
 */
@Data
public class OrderDTO2 {
    private String id;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date startTime;

    @JsonProperty("end_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date endTime;


}
