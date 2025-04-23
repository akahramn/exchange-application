package com.abdullahkahraman.exchange.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@EqualsAndHashCode
@Schema(description = "Paginated response for currency conversion history")
public class HistoryResponse {

    @Schema(
            description = "List of conversion transactions for the current page",
            type = "array",
            implementation = CurrencyDto.class
    )
    private List<CurrencyDto> content;

    @Schema(description = "Current page number (0-based index)", example = "0")
    private int pageNumber;

    @Schema(description = "Size of each page", example = "10")
    private int pageSize;

    @Schema(description = "Total number of conversion transactions", example = "42")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    public HistoryResponse(Page<CurrencyDto> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
