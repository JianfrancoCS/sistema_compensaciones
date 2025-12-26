package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.model.company.*;
import com.agropay.core.sunat.models.CompanyExternalInfo;

import java.util.Optional;

public interface ICompanyUseCase {
    CommandCompanyResponse create(CreateCompanyRequest createCompanyRequest);

    Optional<CompanyDTO> getPrimaryCompany();

    CompanyExternalInfo fetchExternalCompany(String ruc);

    CommandCompanyResponse updatePrimaryCompany(UpdateCompanyRequest updateCompanyRequest);

    CommandCompanyResponse updateMaxMonthlyWorkingHours(UpdateMaxMonthlyWorkingHoursRequest request);

    CommandCompanyResponse updatePaymentIntervalDays(UpdatePaymentIntervalDaysRequest request);

    CommandCompanyResponse updatePayrollDeclarationDay(UpdatePayrollDeclarationDayRequest request);

    CompanyEntity getPrimaryCompanyEntity();

}
