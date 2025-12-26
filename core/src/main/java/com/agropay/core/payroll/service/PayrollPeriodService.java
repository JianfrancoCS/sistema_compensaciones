package com.agropay.core.payroll.service;

import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.application.usecase.ICompanyUseCase;
import com.agropay.core.payroll.service.usecase.IPayrollPeriodService;
import com.agropay.core.payroll.mapper.PayrollPeriodMapper;
import com.agropay.core.payroll.domain.PayrollPeriodEntity;
import com.agropay.core.payroll.model.period.CommandPayrollPeriodResponse;
import com.agropay.core.payroll.model.period.CreatePayrollPeriodRequest;
import com.agropay.core.payroll.model.period.PayrollPeriodSelectOptionDTO;
import com.agropay.core.payroll.persistence.IPayrollPeriodRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollPeriodService implements IPayrollPeriodService {

    private final IPayrollPeriodRepository payrollPeriodRepository;
    private final IPayrollRepository payrollRepository; // Injected to check for assigned payrolls
    private final ICompanyUseCase companyUseCase;
    private final PayrollPeriodMapper payrollPeriodMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommandPayrollPeriodResponse> getAllPeriods() {
        return payrollPeriodRepository.findAll().stream()
            .map(payrollPeriodMapper::toCommandResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollPeriodSelectOptionDTO> getSelectOptions() {
        return payrollPeriodRepository.findAll().stream()
            .map(period -> new PayrollPeriodSelectOptionDTO(
                period.getPublicId(),
                String.format("%d-%02d (%s to %s)", period.getYear(), period.getMonth(), period.getPeriodStart(), period.getPeriodEnd()),
                payrollRepository.existsByPeriodId(period.getId())
            ))
            .collect(Collectors.toList());
    }

    @Override
    public CommandPayrollPeriodResponse createPeriod(CreatePayrollPeriodRequest request) {
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();

        LocalDate periodStart;

        if (request.explicitStartDate() != null) {
            periodStart = request.explicitStartDate();
        } else {
            periodStart = payrollPeriodRepository.findFirstByOrderByPeriodEndDesc()
                .map(lastPeriod -> lastPeriod.getPeriodEnd().plusDays(1))
                .orElseThrow(() -> new BusinessValidationException("exception.payroll.period.no-previous-period"));
        }

        LocalDate periodEnd = periodStart.plusDays((long) company.getPaymentIntervalDays() - 1);

        // Calculate declaration date based on the fixed day of the month
        LocalDate declarationDate;
        LocalDate potentialDeclarationDate = periodEnd.withDayOfMonth(company.getPayrollDeclarationDay());

        if (potentialDeclarationDate.isBefore(periodEnd)) {
            // If the declaration day in the current month is before the period ends, move to the next month
            declarationDate = periodEnd.plusMonths(1).withDayOfMonth(company.getPayrollDeclarationDay());
        } else {
            declarationDate = potentialDeclarationDate;
        }

        // Calcular el número de período dentro del mes (para períodos semanales)
        // Contar cuántos períodos ya existen en este mes que terminan antes o en la misma fecha
        Short periodYear = (short) periodEnd.getYear();
        Byte periodMonth = (byte) periodEnd.getMonthValue();
        long existingPeriodsCount = payrollPeriodRepository.countByYearAndMonthAndPeriodEndLessThanEqual(
            periodYear, periodMonth, periodEnd);
        Byte periodNumber = (byte) (existingPeriodsCount + 1);

        PayrollPeriodEntity newPeriod = PayrollPeriodEntity.builder()
            .publicId(UUID.randomUUID())
            .year(periodYear) // The period belongs to the year it ends in
            .month(periodMonth) // The period belongs to the month it ends in
            .periodNumber(periodNumber) // Número de período dentro del mes
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .declarationDate(declarationDate)
            .build();

        payrollPeriodRepository.save(newPeriod);

        return payrollPeriodMapper.toCommandResponse(newPeriod);
    }

    @Override
    public void deletePeriod(UUID publicId) {
        PayrollPeriodEntity periodToDelete = payrollPeriodRepository.findByPublicId(publicId)
            .orElseThrow(() -> new IdentifierNotFoundException("exception.payroll.period.not-found",publicId));

        if (payrollRepository.existsByPeriodId(periodToDelete.getId())) {
            throw new BusinessValidationException("exception.payroll.period.no-cant-delete");
        }

        payrollPeriodRepository.softDelete(periodToDelete.getId(), "SYSTEM");
    }
}
