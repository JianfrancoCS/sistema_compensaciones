package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.api.IEmployeeAPI;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.model.employee.*;
import com.agropay.core.shared.utils.PagedResult;
import com.agropay.core.states.models.StateSelectOptionDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IEmployeeUseCase extends IEmployeeAPI {
    CommandEmployeeResponse create(CreateEmployeeRequest request);

    CommandEmployeeResponse update(UUID code, UpdateEmployeeRequest request);

    EmployeeDetailsDTO getByCode(UUID code);

    CommandEmployeeResponse getCommandResponseByPublicId(UUID publicId);

    PagedResult<EmployeeListDTO> findAllPaged(String documentNumber, UUID subsidiaryId, UUID positionId, Boolean isNational, Pageable pageable);

    List<EmployeeSelectOptionDTO> getSelectOptions(UUID positionPublicId);

    List<OrganizationalChartNodeDTO> getOrganizationalChart(UUID subsidiaryId, Integer levels);

    List<StateSelectOptionDTO> getStatesSelectOptions();

    void updateSubsidiary(UUID employeePublicId, UpdateEmployeeSubsidiaryRequest request);

    void updatePosition(UUID employeePublicId, UpdateEmployeePositionRequest request);

    void activateEmployee(UUID code);

    void deactivateEmployee(UUID code);

    EmployeeSearchResponse searchByDocumentNumber(String documentNumber);

    void updateCustomSalary(String documentNumber, java.math.BigDecimal newSalary);

    EmployeeMeResponse getMyInfo(String username);

    EmployeeCacheResponse getEmployeeCacheByDocumentNumber(String documentNumber);

}
