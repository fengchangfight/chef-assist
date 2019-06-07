package com.chef.assist.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProducerViewItem {
    private String id;
    private String orderNumber;
    private String tableNumber;
    private String dishName;

    public ProducerViewItem ellipsis() {
        String rawOrderNumber = getOrderNumber();
        StringBuffer buf = new StringBuffer(rawOrderNumber);
        int start = 0;
        int end = rawOrderNumber.length()-4;
        buf.replace(start, end, "");
        this.setOrderNumber("o_**"+buf.toString());
        return this;
    }
}
