package com.agropay.core.hiring.model.contract;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CommandContractResponse(
        UUID publicId,
        String contractNumber,
        String personDocumentNumber,
        LocalDate startDate,
        LocalDate endDate,
        UUID contractTypePublicId,
        StateInfo state,
        SubsidiaryInfo subsidiary,
        PositionInfo position,
        TemplateInfo template,
        List<ContractVariableValuePayload> variables
) {
    public record StateInfo(UUID publicId) {}
    public record SubsidiaryInfo(UUID publicId) {}
    public record PositionInfo(UUID publicId, UUID areaPublicId) {}
    public record TemplateInfo(UUID publicId, UUID contractTypePublicId) {}
}
