package com.agropay.core.attendance.mapper;

import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.attendance.model.marking.MarkingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IMarkingMapper {

    @Mapping(target = "entryType", expression = "java(entity.getIsEntry() ? \"ENTRADA\" : \"SALIDA\")")
    @Mapping(target = "markingReasonName", source = "markingReason.name")
    @Mapping(target = "subsidiaryName", source = "marking.subsidiary.name")
    @Mapping(target = "markedAt", source = "markedAt")
    MarkingResponse toMarkingResponse(MarkingDetailEntity entity);
}