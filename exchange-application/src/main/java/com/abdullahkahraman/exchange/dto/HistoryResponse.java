package com.abdullahkahraman.exchange.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@EqualsAndHashCode
public class HistoryResponse {

    private List<CurrencyDto> content;

    private int pageNumber;

    private int pageSize;

    private long totalElements;

    private int totalPages;

    public HistoryResponse(Page<CurrencyDto> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
