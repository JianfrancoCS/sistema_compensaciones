package com.agropay.core.states.mapper;

import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.states.models.StateSelectOptionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IStateMapper {

    @Mapping(target = "isDefault", source = "isDefault")
    List<StateSelectOptionDTO> toSelectOptionDto(List<StateEntity> entities);
}
