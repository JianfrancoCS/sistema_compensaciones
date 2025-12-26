package com.agropay.core.attendance.model.attendance;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record AttendanceSummaryDTO(
    UUID subsidiaryPublicId,
    String subsidiaryName,
    LocalDate date,

    // Empleados (internos)
    Long employeesWithEntry,
    Long employeesWithExit,
    Long employeesCurrentlyInside,  // Con entrada pero sin salida

    // Personas externas
    Long externalsWithEntry,
    Long externalsWithExit,
    Long externalsCurrentlyInside,  // Con entrada pero sin salida

    // Totales
    Long totalWithEntry,
    Long totalWithExit,
    Long totalCurrentlyInside
) {

    public static AttendanceSummaryDTO create(
            UUID subsidiaryPublicId,
            String subsidiaryName,
            LocalDate date,
            Long employeesWithEntry,
            Long employeesWithExit,
            Long externalsWithEntry,
            Long externalsWithExit
    ) {
        Long employeesCurrentlyInside = employeesWithEntry - employeesWithExit;
        Long externalsCurrentlyInside = externalsWithEntry - externalsWithExit;
        Long totalWithEntry = employeesWithEntry + externalsWithEntry;
        Long totalWithExit = employeesWithExit + externalsWithExit;
        Long totalCurrentlyInside = employeesCurrentlyInside + externalsCurrentlyInside;

        return AttendanceSummaryDTO.builder()
                .subsidiaryPublicId(subsidiaryPublicId)
                .subsidiaryName(subsidiaryName)
                .date(date)
                .employeesWithEntry(employeesWithEntry)
                .employeesWithExit(employeesWithExit)
                .employeesCurrentlyInside(employeesCurrentlyInside)
                .externalsWithEntry(externalsWithEntry)
                .externalsWithExit(externalsWithExit)
                .externalsCurrentlyInside(externalsCurrentlyInside)
                .totalWithEntry(totalWithEntry)
                .totalWithExit(totalWithExit)
                .totalCurrentlyInside(totalCurrentlyInside)
                .build();
    }
}