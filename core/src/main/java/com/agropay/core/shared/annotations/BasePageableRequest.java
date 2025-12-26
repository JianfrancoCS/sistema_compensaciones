package com.agropay.core.shared.annotations;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public abstract class BasePageableRequest {

    @Min(value = 0, message = "{validation.page.min}")
    private int page = 0;
    @Min(value = 1, message = "{validation.size.range}")
    @Max(value = 250, message = "{validation.size.range}")
    private int size = 10;

    @Pattern(regexp = "^(?i)(ASC|DESC)$", message = "{validation.sortDirection.invalid}")
    private String sortDirection = "ASC";
    private String sortBy = "createdAt";

    public Pageable toPageable() {
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );
        return PageRequest.of(page, size, sort);
    }

}