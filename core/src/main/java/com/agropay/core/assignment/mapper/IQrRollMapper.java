package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.QrRollEntity;
import com.agropay.core.assignment.model.qrroll.*;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IQrRollMapper {

    CommandQrRollResponse toResponse(QrRollEntity entity);

    void updateEntityFromRequest(UpdateQrRollRequest request, @MappingTarget QrRollEntity entity);

    @Mapping(target = "totalQrCodes", expression = "java(totalQrCodes)")
    @Mapping(target = "unprintedQrCodes", expression = "java(unprintedQrCodes)")
    QrRollListDTO toListDTO(QrRollEntity entity, Long totalQrCodes, Long unprintedQrCodes);
}