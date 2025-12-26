package com.agropay.core.organization.api;

import com.agropay.core.organization.domain.EmployeeEntity;

import java.util.Optional;
import java.util.UUID;

public interface IEmployeeAPI {

    Optional<EmployeeEntity> findByPersonDocumentNumber(String documentNumber);

    EmployeeEntity findByPublicId(UUID publicId);

    EmployeeEntity findByDocumentNumber(String documentNumber);
}
