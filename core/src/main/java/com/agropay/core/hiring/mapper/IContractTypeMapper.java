package com.agropay.core.hiring.mapper;

import com.agropay.core.hiring.domain.ContractTypeEntity;
import com.agropay.core.hiring.model.contracttype.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IContractTypeMapper {

    ContractTypeEntity toEntity(CreateContractTypeRequest request);

    void updateEntityFromRequest(UpdateContractTypeRequest request, @MappingTarget ContractTypeEntity entity);

    // Removed ContractTypeDetailsDTO toDetailsDTO(ContractTypeEntity entity);

    CommandContractTypeResponse toCommandResponse(ContractTypeEntity entity);

    ContractTypeListDTO toListDTO(ContractTypeEntity entity);

    List<ContractTypeListDTO> toListDTOs(List<ContractTypeEntity> entities);

    ContractTypeSelectOptionDTO toSelectOptionDTO(ContractTypeEntity entity);

    List<ContractTypeSelectOptionDTO> toSelectOptionDTOs(List<ContractTypeEntity> entities);

    default PagedResult<ContractTypeListDTO> toPagedDTO(Page<ContractTypeEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
