package com.oem.evwarranty.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiPagination {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;

    public static ApiPagination fromPage(Page<?> page) {
        return ApiPagination.builder()
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
