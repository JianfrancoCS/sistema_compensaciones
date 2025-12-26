package com.agropay.core.organization.model.signer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request para crear un responsable de firma
 */
public record CreateSubsidiarySignerRequest(
    UUID subsidiaryPublicId, // null = a nivel de empresa
    
    @NotBlank(message = "El número de documento del empleado responsable es requerido")
    @Size(min = 8, max = 15, message = "El número de documento debe tener entre 8 y 15 caracteres")
    String responsibleEmployeeDocumentNumber,
    
    @NotBlank(message = "El cargo del responsable es requerido")
    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    String responsiblePosition,
    
    String signatureImageUrl, // URL de la imagen de firma (opcional)
    
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    String notes
) {}

