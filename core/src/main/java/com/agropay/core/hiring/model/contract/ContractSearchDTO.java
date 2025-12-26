package com.agropay.core.hiring.model.contract;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContractSearchDTO(
        UUID publicId,
        String contractNumber,
        LocalDate startDate,
        LocalDate endDate,
        String personDocumentNumber,
        String personFullName,
        String contractTypeName,
        String stateName,
        List<ContractImageDTO> imageUrls
) {

    public record ContractImageDTO(
            UUID publicId,
            String url,
            Integer order
    ) {}
}
