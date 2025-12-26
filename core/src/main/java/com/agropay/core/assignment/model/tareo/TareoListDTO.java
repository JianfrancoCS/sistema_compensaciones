package com.agropay.core.assignment.model.tareo;

import java.time.LocalDateTime;
import java.util.UUID;

public record TareoListDTO(
        UUID publicId,
        String laborName,
        String loteName,
        String loteSubsidiaryName,
        Long employeeCount,
        Boolean isProcessed,
        LocalDateTime createdAt
) {}