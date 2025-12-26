package com.agropay.core.assignment.model.tareo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TareoDailyDTO(
    UUID publicId,
    LocalDate tareoDate,
    String laborName,
    UUID laborPublicId,
    String loteName,
    UUID lotePublicId,
    String subsidiaryName,
    UUID subsidiaryPublicId,
    String supervisorName,
    String supervisorDocumentNumber,
    String scannerName,
    String scannerDocumentNumber,
    Long employeeCount,
    Boolean isCalculated,
    LocalDateTime createdAt
) {}

