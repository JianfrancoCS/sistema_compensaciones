package com.agropay.core.organization.mapper;

import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.model.company.CompanyDTO;
import com.agropay.core.organization.model.company.CreateCompanyRequest;
import com.agropay.core.organization.model.company.CommandCompanyResponse;
import com.agropay.core.organization.model.company.UpdateCompanyRequest;
import com.agropay.core.sunat.models.CompanyExternalInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ICompanyMapper {

    CompanyEntity toEntity(CreateCompanyRequest request);

    void updateEntityFromRequest(UpdateCompanyRequest request, @MappingTarget CompanyEntity entity);

    CommandCompanyResponse toResponse(CompanyEntity companyEntity);

    CompanyDTO toDTO(CompanyExternalInfo externalInfo);

    CompanyDTO toDTO(CompanyEntity companyEntity);
}
