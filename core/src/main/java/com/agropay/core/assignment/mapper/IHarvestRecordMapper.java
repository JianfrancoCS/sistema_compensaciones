package com.agropay.core.assignment.mapper;

import com.agropay.core.assignment.domain.HarvestRecordEntity;
import com.agropay.core.assignment.model.harvest.*;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IHarvestRecordMapper {

    @Mapping(target = "qrCode", source = "qrCode.id")
    CommandHarvestRecordResponse toResponse(HarvestRecordEntity entity);
}
