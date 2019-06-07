package com.chef.assist.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaginationWrapper {
    private int total;
    private int pageSize;
    private int currentPage;
    private Object data;

    public PaginationWrapper(int total, int pageSize, int currentPage, Object data) {
        this.total = total;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.data = data;
    }
}
