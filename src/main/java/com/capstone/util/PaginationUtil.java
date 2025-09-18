package com.capstone.util;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.model.PaginationMetadata;
import org.springframework.data.domain.Page;

public class PaginationUtil {

    public static <T> PaginatedResponseDto<T> toPaginatedResponse(Page<T> pageData) {
        return PaginatedResponseDto.<T>builder()
                .items(pageData.getContent())
                .pagination(PaginationMetadata.builder()
                        .page(pageData.getNumber())
                        .size(pageData.getSize())
                        .totalElements(pageData.getTotalElements())
                        .totalPages(pageData.getTotalPages())
                        .hasNext(pageData.hasNext())
                        .hasPrevious(pageData.hasPrevious())
                        .build())
                .build();
    }
}
