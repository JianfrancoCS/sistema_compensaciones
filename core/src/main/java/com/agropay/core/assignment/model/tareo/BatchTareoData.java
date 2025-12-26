package com.agropay.core.assignment.model.tareo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BatchTareoData(
        @NotBlank(message = "{validation.tareo.temporal-id.notblank}")
        String temporalId,

        @NotNull(message = "{validation.tareo.labor-id.notnull}")
        UUID laborPublicId,

        // Opcional: NULL para tareos administrativos que no requieren lote
        UUID lotePublicId,

        @NotBlank(message = "{validation.tareo.supervisor-document-number.notblank}")
        String supervisorDocumentNumber,

        String scannerDocumentNumber,

        @NotEmpty(message = "{validation.tareo.employees.notempty}")
        @Valid
        List<BatchEmployeeData> employees,
        
        // Flag para indicar si es una carga de cierre (true) o carga normal (false)
        Boolean isClosing,
        
        // Motivo de cierre (requerido cuando isClosing = true, opcional cuando isClosing = false)
        UUID closingMotivePublicId // Puede ser null cuando isClosing = false
) {
    // Constructor con valor por defecto para isClosing
    public BatchTareoData {
        if (isClosing == null) {
            isClosing = false; // Por defecto es carga normal
        }
    }
}