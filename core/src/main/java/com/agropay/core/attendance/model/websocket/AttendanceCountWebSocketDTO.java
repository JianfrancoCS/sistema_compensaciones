package com.agropay.core.attendance.model.websocket;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AttendanceCountWebSocketDTO(
    UUID subsidiaryPublicId,
    LocalDate date,
    Long inside,    // Personas actualmente dentro
    Long outside,   // Personas que salieron
    Long total,     // Total de entradas del día
    LocalDateTime timestamp  // Momento de la actualización
) {

    public static AttendanceCountWebSocketDTO create(
            UUID subsidiaryPublicId,
            LocalDate date,
            Long totalEntries,
            Long totalExits
    ) {
        return AttendanceCountWebSocketDTO.builder()
                .subsidiaryPublicId(subsidiaryPublicId)
                .date(date)
                .inside(totalEntries - totalExits)
                .outside(totalExits)
                .total(totalEntries)
                .timestamp(LocalDateTime.now())
                .build();
    }
}