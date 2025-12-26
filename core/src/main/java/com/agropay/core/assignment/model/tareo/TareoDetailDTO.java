package com.agropay.core.assignment.model.tareo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record TareoDetailDTO(
        UUID publicId,
        LaborInfo labor,
        LoteInfo lote,
        SupervisorInfo supervisor,
        AcopiadorInfo acopiador,
        String createdBy,
        LocalDateTime createdAt,
        List<EmployeeItem> employees
) {
    public record LaborInfo(String name, boolean isPiecework) {}
    public record LoteInfo(String loteName, String subsidiaryName) {}
    public record SupervisorInfo(String documentNumber, String fullName) {}
    public record AcopiadorInfo(String documentNumber, String fullName) {}

    public record EmployeeItem(
            UUID publicId,
            String documentNumber,
            String fullName,
            String position,
            LocalTime entryTime,
            LocalTime exitTime,
            ProductivityInfo productivity
    ) {}
    
    public record ProductivityInfo(
            BigDecimal productivityPercentage,
            Long harvestCount,
            BigDecimal minTaskRequirement,
            String unitOfMeasure, // Unidad de medida de la labor (ej: "Jarras", "Jabas")
            CollectorInfo collector
    ) {}
    
    public record CollectorInfo(
            String documentNumber,
            String fullName
    ) {}
}
