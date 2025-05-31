package com.one.core.application.dto.tenant.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class PageableResponse<T> {

    private List<T> results;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;

    public PageableResponse(Page<T> springDataPage) {
        this.results = springDataPage.getContent();
        this.totalPages = springDataPage.getTotalPages();
        this.totalElements = springDataPage.getTotalElements();
        this.number = springDataPage.getNumber();
        this.size = springDataPage.getSize();
    }
}
