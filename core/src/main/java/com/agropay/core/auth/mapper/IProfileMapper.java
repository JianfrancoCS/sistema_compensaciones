package com.agropay.core.auth.mapper;

import com.agropay.core.auth.domain.ProfileEntity;
import com.agropay.core.auth.model.profile.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    ProfileEntity toEntity(CreateProfileRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntityFromRequest(UpdateProfileRequest request, @MappingTarget ProfileEntity entity);

    CommandProfileResponse toResponse(ProfileEntity entity);

    ProfileDetailsDTO toDetailsDTO(ProfileEntity entity);

    ProfileListDTO toListDTO(ProfileEntity entity);

    List<ProfileListDTO> toListDTOs(List<ProfileEntity> entities);

    default PagedResult<ProfileListDTO> toPagedDTO(Page<ProfileEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}

