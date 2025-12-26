package com.agropay.core.organization.model.signer;

import jakarta.validation.constraints.Size;

/**
 * Request para actualizar un responsable de firma
 */
public record UpdateSubsidiarySignerRequest(
    String responsibleEmployeeDocumentNumber, // Opcional: solo si se cambia el empleado
    
    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    String responsiblePosition, // Opcional
    
    String signatureImageUrl, // Opcional
    
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    String notes // Opcional
) {}

