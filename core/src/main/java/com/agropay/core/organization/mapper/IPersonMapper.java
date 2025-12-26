package com.agropay.core.organization.mapper;

import com.agropay.core.organization.api.PersonApiDTO;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.model.person.CommandPersonResponse;
import com.agropay.core.organization.model.person.PersonDetailsDTO;
import com.agropay.core.organization.model.person.PersonListDTO;
import com.agropay.core.shared.utils.PagedResult; // Added missing import
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IPersonMapper {
    List<CommandPersonResponse> toResponseList(List<PersonEntity> entities);

    PersonApiDTO toDTO(PersonEntity entity);

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    PersonListDTO toListDTO(PersonEntity entity);
    List<PersonListDTO> toListDTO(List<PersonEntity> entities);

    @Mapping(source = "district.publicId", target = "districtPublicId")
    PersonDetailsDTO toDetailsDTO(PersonEntity entity);

    CommandPersonResponse toCommandResponse(PersonEntity entity);

    default PagedResult<PersonListDTO> toPagedDTO(Page<PersonEntity> page) {
        Page<PersonListDTO> mapped = page.map(this::toListDTO);
        return new PagedResult<>(mapped);
    }
}
