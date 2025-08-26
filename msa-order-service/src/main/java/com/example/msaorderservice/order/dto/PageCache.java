package com.example.msaorderservice.order.dto;

import lombok.*;
import org.springframework.data.domain.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageCache<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private String sort;

    public static <T> PageCache<T> fromPage(Page<T> p) {
        return new PageCache<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getSort() == null ? null : p.getSort().toString()
        );
    }

    public Page<T> toPage() {
        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());
        return new PageImpl<>(content, pageable, totalElements);
    }
}