package com.agropay.core.organization.model.signer;

import java.util.UUID;

/**
 * DTO para listar subsidiarias con su responsable de firma asignado
 */
public record SubsidiarySignerListDTO(
    UUID subsidiaryPublicId,
    String subsidiaryName,
    String responsibleEmployeeDocumentNumber,
    String responsibleEmployeeName,
    String responsiblePosition,
    String signatureImageUrl,
    Boolean hasSigner
) {}

