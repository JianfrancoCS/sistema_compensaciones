package com.agropay.core.organization.mapper;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.model.employee.*;
import com.agropay.core.shared.utils.PagedResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IEmployeeMapper {

    @Mapping(target = "personDocumentNumber", source = "documentNumber") // Corrected mapping: request.documentNumber -> entity.personDocumentNumber
    @Mapping(target = "person", source = "request")
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "subsidiary", ignore = true)
    @Mapping(target = "position", ignore = true)
    EmployeeEntity toEntity(CreateEmployeeRequest request);

    @Mapping(source = "person.documentNumber", target = "documentNumber")
    @Mapping(source = "person.names", target = "names")
    @Mapping(source = "person.paternalLastname", target = "paternalLastname")
    @Mapping(source = "person.maternalLastname", target = "maternalLastname")
    @Mapping(source = "subsidiary.name", target = "subsidiaryName")
    @Mapping(source = "position.name", target = "positionName")
    @Mapping(target = "isNational", expression = "java(com.agropay.core.organization.constant.DocumentTypeEnum.DNI.getCode().equals(entity.getPerson().getDocumentType().getCode()))")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    EmployeeListDTO toListDTO(EmployeeEntity entity);

    List<EmployeeListDTO> toListDTOs(List<EmployeeEntity> entities);

    @Mapping(source = "person.names", target = "names")
    @Mapping(source = "person.paternalLastname", target = "paternalLastname")
    @Mapping(source = "person.maternalLastname", target = "maternalLastname")
    @Mapping(source = "person.dob", target = "dob")
    @Mapping(source = "subsidiary.name", target = "subsidiaryName")
    @Mapping(source = "position.name", target = "positionName")
    @Mapping(source = "position.area.name", target = "areaName")
    @Mapping(target = "salary", expression = "java(entity.getCustomSalary() != null ? entity.getCustomSalary() : entity.getPosition().getSalary())")
    @Mapping(source = "manager.code", target = "manager.code")
    @Mapping(source = "manager.person.names", target = "manager.fullName")
    EmployeeDetailsDTO toDetailsDTO(EmployeeEntity entity);

    @Mapping(source = "person.documentNumber", target = "documentNumber")
    @Mapping(source = "person.names", target = "names")
    @Mapping(source = "person.paternalLastname", target = "paternalLastname")
    @Mapping(source = "person.maternalLastname", target = "maternalLastname")
    @Mapping(source = "subsidiary.publicId", target = "subsidiaryPublicId")
    @Mapping(source = "position.publicId", target = "positionPublicId")
    @Mapping(source = "manager.code", target = "managerPublicId")
    CommandEmployeeResponse toResponse(EmployeeEntity entity);

    @Mapping(source = "person.names", target = "fullName")
    EmployeeSelectOptionDTO toSelectOptionDTO(EmployeeEntity entity);

    List<EmployeeSelectOptionDTO> toSelectOptionDTOs(List<EmployeeEntity> entities);

    default PagedResult<EmployeeListDTO> toPagedDTO(Page<EmployeeEntity> page) {
        return new PagedResult<>(page.map(this::toListDTO));
    }
}
