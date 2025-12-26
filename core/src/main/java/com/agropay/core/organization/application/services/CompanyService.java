package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.ICompanyUseCase;
import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.ProvidersExternalException;
import com.agropay.core.organization.mapper.ICompanyMapper;
import com.agropay.core.organization.model.company.*;
import com.agropay.core.organization.persistence.ICompanyRepository;
import com.agropay.core.sunat.application.ICompanyExternalProvider;
import com.agropay.core.sunat.models.CompanyExternalInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService implements ICompanyUseCase {

    private final ICompanyRepository companyRepository;
    private final ICompanyMapper companyMapper;
    private final ICompanyExternalProvider companyExternalService;

    @Override
    @Transactional
    public CommandCompanyResponse create(CreateCompanyRequest request) {
        log.info("Attempting to create the primary company with RUC: {}", request.ruc());
        this.checkCompanyBaseDefined();
        CompanyEntity companyEntity = companyMapper.toEntity(request);
        CompanyEntity savedEntity = companyRepository.save(companyEntity);
        log.info("Successfully created primary company with ID: {}", savedEntity.getId());
        return companyMapper.toResponse(savedEntity);
    }

    private void checkCompanyBaseDefined() {
        if (companyRepository.count() > 0) {
            log.warn("Attempted to create a primary company, but one is already defined.");
            throw new BusinessValidationException("exception.organization.company.base-already-defined");
        }
    }

    @Override
    @Transactional
    public CommandCompanyResponse updatePrimaryCompany(UpdateCompanyRequest request) {
        CompanyEntity existingCompany = this.getPrimaryCompanyEntity();
        companyMapper.updateEntityFromRequest(request, existingCompany);
        CompanyEntity updatedEntity = companyRepository.save(existingCompany);
        log.info("Successfully updated primary company with ID: {}", updatedEntity.getId());
        return companyMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public CommandCompanyResponse updateMaxMonthlyWorkingHours(UpdateMaxMonthlyWorkingHoursRequest request) {
        CompanyEntity existingCompany = this.getPrimaryCompanyEntity();
        existingCompany.setMaxMonthlyWorkingHours(request.maxMonthlyWorkingHours());
        CompanyEntity updatedEntity = companyRepository.save(existingCompany);
        log.info("Successfully updated max monthly working hours for company ID: {}", updatedEntity.getId());
        return companyMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public CommandCompanyResponse updatePaymentIntervalDays(UpdatePaymentIntervalDaysRequest request) {
        CompanyEntity existingCompany = this.getPrimaryCompanyEntity();
        existingCompany.setPaymentIntervalDays(request.paymentIntervalDays());
        CompanyEntity updatedEntity = companyRepository.save(existingCompany);
        log.info("Successfully updated payment interval days for company ID: {}", updatedEntity.getId());
        return companyMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public CommandCompanyResponse updatePayrollDeclarationDay(UpdatePayrollDeclarationDayRequest request) {
        CompanyEntity existingCompany = this.getPrimaryCompanyEntity();
        existingCompany.setPayrollDeclarationDay(request.payrollDeclarationDay());
        CompanyEntity updatedEntity = companyRepository.save(existingCompany);
        log.info("Successfully updated payroll declaration day for company ID: {}", updatedEntity.getId());
        return companyMapper.toResponse(updatedEntity);
    }

    @Override
    public Optional<CompanyDTO> getPrimaryCompany() {
        log.info("Fetching the primary company.");
        return companyRepository.getPrimaryCompany()
                .map(companyMapper::toDTO);
    }

    @Override
    public CompanyExternalInfo fetchExternalCompany(String ruc) {
        log.info("Fetching external company information for RUC: {}", ruc);
        return companyExternalService.getCompany(ruc)
                .orElseThrow(() -> {
                    log.warn("Company with RUC {} not found in any external provider.", ruc);
                    return new IdentifierNotFoundException("exception.external-service.company.not-found", ruc);
                });
    }

    @Override
    public CompanyEntity getPrimaryCompanyEntity() {
        log.debug("Attempting to retrieve the primary company entity.");
        return companyRepository.getPrimaryCompany()
                .orElseThrow(() -> {
                    log.warn("Attempted to retrieve the primary company, but it has not been defined yet.");
                    return new BusinessValidationException("exception.organization.company.not-found");
                });
    }
}
