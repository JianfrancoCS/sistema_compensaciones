package com.agropay.core.organization.model.signer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO con detalles completos de un responsable de firma
 */
public record SubsidiarySignerDetailsDTO(
    UUID publicId,
    UUID subsidiaryPublicId,
    String subsidiaryName,
    String responsibleEmployeeDocumentNumber,
    String responsibleEmployeeName,
    String responsiblePosition,
    String signatureImageUrl,
    String notes,
    LocalDateTime createdAt,
    String createdBy
) {}

