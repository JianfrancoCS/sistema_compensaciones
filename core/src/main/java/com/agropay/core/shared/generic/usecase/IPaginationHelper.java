package com.agropay.core.shared.generic.usecase;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface IPaginationHelper {
    List<String> getAllowedSortFields();
    default String getDefaultSortField() {
        return "updatedAt";
    }
    default Sort.Direction getDefaultSortDirection() {
        return Sort.Direction.ASC;
    }
    default Pageable createValidatedPageable(int page, int size, String sortDirection, String sortBy) {
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            direction = getDefaultSortDirection();
        }
        List<String> allowedSortFields = getAllowedSortFields();
        if (sortBy == null || !allowedSortFields.contains(sortBy)) {
            sortBy = getDefaultSortField();
        }
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 10;

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
