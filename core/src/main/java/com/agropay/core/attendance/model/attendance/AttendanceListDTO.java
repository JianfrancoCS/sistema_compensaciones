package com.agropay.core.attendance.model.attendance;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AttendanceListDTO(
    UUID publicId,
    String personDocumentNumber,
    String personFullName,
    String markingReasonName,
    boolean isEmployee,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate markingDate,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime entryTime,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime exitTime,

    String subsidiaryName
) {
}