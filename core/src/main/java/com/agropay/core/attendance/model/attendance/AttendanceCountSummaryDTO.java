package com.agropay.core.attendance.model.attendance;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record AttendanceCountSummaryDTO(
    UUID subsidiaryPublicId,
    String subsidiaryName,
    LocalDate date,
    Long inside,    // Personas actualmente dentro (entradas - salidas)
    Long outside,   // Personas que salieron (total de salidas)
    Long total      // Total de entradas del día
) {

    public static AttendanceCountSummaryDTO create(
            UUID subsidiaryPublicId,
            String subsidiaryName,
            LocalDate date,
            Long totalEntries,
            Long totalExits
    ) {
        Long inside = totalEntries - totalExits;
        Long outside = totalExits;
        Long total = totalEntries;

        return AttendanceCountSummaryDTO.builder()
                .subsidiaryPublicId(subsidiaryPublicId)
                .subsidiaryName(subsidiaryName)
                .date(date)
                .inside(inside)
                .outside(outside)
                .total(total)
                .build();
    }
}