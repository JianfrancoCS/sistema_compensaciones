package com.agropay.core.attendance.model.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record EmployeeAttendanceCheckDTO(
    boolean hasAttendance,
    boolean hasEntry,
    boolean hasExit,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate checkDate,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime entryTime,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime exitTime,

    String personDocumentNumber,
    String personFullName
) {
}