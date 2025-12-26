package com.agropay.core.hiring.model.addendum;

import java.time.LocalDate;
import java.util.UUID;

public record AddendumDetailsDTO(
    UUID publicId,
    String addendumNumber,
    LocalDate startDate,
    String content,
    String variables,
    UUID contractPublicId,
    String contractNumber,
    UUID addendumTypePublicId,
    String addendumTypeName,
    UUID statePublicId,
    String stateName,
    String imageUrl
) {}