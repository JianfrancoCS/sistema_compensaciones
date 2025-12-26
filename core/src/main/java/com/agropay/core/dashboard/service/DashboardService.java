package com.agropay.core.dashboard.service;

import com.agropay.core.assignment.persistence.ITareoRepository;
import com.agropay.core.dashboard.model.*;
import com.agropay.core.organization.persistence.IEmployeeRepository;
import com.agropay.core.organization.persistence.ISubsidiaryRepository;
import com.agropay.core.payroll.persistence.IPayrollPeriodRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.attendance.persistence.IMarkingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final IEmployeeRepository employeeRepository;
    private final ISubsidiaryRepository subsidiaryRepository;
    private final IPayrollRepository payrollRepository;
    private final IPayrollPeriodRepository payrollPeriodRepository;
    private final ITareoRepository tareoRepository;
    private final IMarkingRepository markingRepository;

    public DashboardStatsDTO getStats(UUID subsidiaryPublicId, UUID periodPublicId, LocalDate dateFrom, LocalDate dateTo) {
        log.debug("Getting dashboard stats with filters - subsidiary: {}, period: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, periodPublicId, dateFrom, dateTo);

        Long totalEmployees = getTotalEmployees(subsidiaryPublicId);
        Long totalPayrolls = getTotalPayrolls(subsidiaryPublicId, periodPublicId);
        Double totalPayrollsAmount = getTotalPayrollsAmount(subsidiaryPublicId, periodPublicId);
        Long activeSubsidiaries = getActiveSubsidiaries();
        Long totalTareos = getTotalTareos(subsidiaryPublicId, dateFrom, dateTo);
        Long processedTareos = getProcessedTareos(subsidiaryPublicId, dateFrom, dateTo);
        Long pendingPayrolls = getPendingPayrolls(subsidiaryPublicId, periodPublicId);

        return new DashboardStatsDTO(
                totalEmployees,
                totalPayrolls,
                totalPayrollsAmount,
                activeSubsidiaries,
                totalTareos,
                processedTareos,
                pendingPayrolls
        );
    }

    public List<PayrollsByStatusDTO> getPayrollsByStatus(UUID subsidiaryPublicId, UUID periodPublicId) {
        log.debug("Getting payrolls by status - subsidiary: {}, period: {}", subsidiaryPublicId, periodPublicId);

        List<Object[]> results = payrollRepository.getPayrollsByStatusGrouped(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                periodPublicId != null ? getPeriodId(periodPublicId) : null
        );

        return results.stream()
                .map(row -> new PayrollsByStatusDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    public List<EmployeesBySubsidiaryDTO> getEmployeesBySubsidiary(UUID subsidiaryPublicId) {
        log.debug("Getting employees by subsidiary - subsidiary: {}", subsidiaryPublicId);

        List<Object[]> results = employeeRepository.getEmployeesBySubsidiaryGrouped(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null
        );

        return results.stream()
                .map(row -> new EmployeesBySubsidiaryDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<PayrollsByPeriodDTO> getPayrollsByPeriod(UUID subsidiaryPublicId, LocalDate dateFrom, LocalDate dateTo) {
        log.debug("Getting payrolls by period - subsidiary: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, dateFrom, dateTo);

        List<Object[]> results = payrollRepository.getPayrollsByPeriodGrouped(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                dateFrom,
                dateTo
        );

        return results.stream()
                .map(row -> new PayrollsByPeriodDTO(
                        String.format("%d-%02d", ((Number) row[0]).intValue(), ((Number) row[1]).intValue()),
                        ((Number) row[2]).longValue(),
                        row[3] != null ? ((Number) row[3]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    public List<TareosByLaborDTO> getTareosByLabor(UUID subsidiaryPublicId, LocalDate dateFrom, LocalDate dateTo) {
        log.debug("Getting tareos by labor - subsidiary: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, dateFrom, dateTo);

        List<Object[]> results = tareoRepository.getTareosByLaborGrouped(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                dateFrom,
                dateTo
        );

        return results.stream()
                .map(row -> new TareosByLaborDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<AttendanceTrendDTO> getAttendanceTrend(UUID subsidiaryPublicId, LocalDate dateFrom, LocalDate dateTo) {
        log.debug("Getting attendance trend - subsidiary: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, dateFrom, dateTo);

        List<Object[]> results = markingRepository.getAttendanceTrend(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                dateFrom,
                dateTo
        );

        return results.stream()
                .map(row -> {
                    LocalDate date;
                    if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof LocalDate) {
                        date = (LocalDate) row[0];
                    } else {
                        date = ((java.time.temporal.TemporalAccessor) row[0]).query(java.time.temporal.TemporalQueries.localDate());
                    }
                    return new AttendanceTrendDTO(
                            date,
                            ((Number) row[1]).longValue(),
                            ((Number) row[2]).longValue()
                    );
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private Long getTotalEmployees(UUID subsidiaryPublicId) {
        if (subsidiaryPublicId != null) {
            Short subsidiaryId = getSubsidiaryId(subsidiaryPublicId);
            return employeeRepository.countBySubsidiaryId(subsidiaryId);
        }
        return employeeRepository.count();
    }

    private Long getTotalPayrolls(UUID subsidiaryPublicId, UUID periodPublicId) {
        if (subsidiaryPublicId != null || periodPublicId != null) {
            return payrollRepository.countByFilters(
                    subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                    periodPublicId != null ? getPeriodId(periodPublicId) : null
            );
        }
        return payrollRepository.count();
    }

    private Double getTotalPayrollsAmount(UUID subsidiaryPublicId, UUID periodPublicId) {
        Double amount = payrollRepository.getTotalAmountByFilters(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                periodPublicId != null ? getPeriodId(periodPublicId) : null
        );
        return amount != null ? amount : 0.0;
    }

    private Long getActiveSubsidiaries() {
        return subsidiaryRepository.count();
    }

    private Long getTotalTareos(UUID subsidiaryPublicId, LocalDate dateFrom, LocalDate dateTo) {
        return tareoRepository.countByFilters(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                dateFrom,
                dateTo
        );
    }

    private Long getProcessedTareos(UUID subsidiaryPublicId, LocalDate dateFrom, LocalDate dateTo) {
        return tareoRepository.countProcessedByFilters(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                dateFrom,
                dateTo
        );
    }

    private Long getPendingPayrolls(UUID subsidiaryPublicId, UUID periodPublicId) {
        return payrollRepository.countPendingByFilters(
                subsidiaryPublicId != null ? getSubsidiaryId(subsidiaryPublicId) : null,
                periodPublicId != null ? getPeriodId(periodPublicId) : null
        );
    }

    private Short getSubsidiaryId(UUID publicId) {
        return subsidiaryRepository.findByPublicId(publicId)
                .map(s -> s.getId())
                .orElse(null);
    }

    private Integer getPeriodId(UUID publicId) {
        return payrollPeriodRepository.findByPublicId(publicId)
                .map(p -> p.getId())
                .orElse(null);
    }
}

