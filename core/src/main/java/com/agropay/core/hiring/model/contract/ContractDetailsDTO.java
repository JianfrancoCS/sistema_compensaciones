package com.agropay.core.hiring.model.contract;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContractDetailsDTO(
    UUID publicId,
    String contractNumber,
    LocalDate startDate,
    LocalDate endDate,
    String content,
    String variables,
    String personDocumentNumber,
    String personFullName,
    UUID contractTypePublicId,
    String contractTypeName,
    UUID statePublicId,
    String stateName,
    List<ContractImageDTO> imageUrls
) {
    public record ContractImageDTO(
            UUID publicId,
            String url,
            Integer order
    ) {}
}
