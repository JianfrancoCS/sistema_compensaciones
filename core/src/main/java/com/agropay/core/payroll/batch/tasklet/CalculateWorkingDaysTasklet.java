package com.agropay.core.payroll.batch.tasklet;

import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.persistence.ICompanyRepository;
import com.agropay.core.payroll.domain.PayrollConceptAssignmentEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.persistence.IPayrollConceptAssignmentRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.payroll.service.WorkingDaysService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tasklet to fetch the Payroll, calculate its working days,
 * and prepare the necessary context for the subsequent steps.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalculateWorkingDaysTasklet implements Tasklet {

    private final IPayrollRepository payrollRepository;
    private final ICompanyRepository companyRepository;
    private final WorkingDaysService workingDaysService;
    private final IPayrollConceptAssignmentRepository payrollConceptAssignmentRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== Starting Step 0: Calculate Working Days and Pre-load Data ===");

        String payrollPublicIdStr = chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getJobParameters()
            .getString("payrollPublicId");

        UUID payrollPublicId = UUID.fromString(payrollPublicIdStr);
        log.info("PayrollPublicId received: {}", payrollPublicId);

        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Payroll not found with publicId: " + payrollPublicId
            ));

        LocalDate periodStart = payroll.getPeriodStart();
        LocalDate periodEnd = payroll.getPeriodEnd();
        log.info("Payroll found: ID={}, Code={}, From {} to {}", payroll.getId(), payroll.getCode(), periodStart, periodEnd);

        List<LocalDate> workingDays = workingDaysService.getWorkingDays(periodStart, periodEnd);
        int totalWorkingDays = workingDays.size();
        log.info("Working days calculated: {} days", totalWorkingDays);

        // 4. Get the single CompanyEntity for configuration
        CompanyEntity company = companyRepository.getPrimaryCompany()
            .orElseThrow(() -> new IllegalStateException("Company configuration not found."));
        log.info("Company configuration loaded: ID={}", company.getId());

        // 5. Pre-load payroll concept assignments and sort them by priority
        final List<PayrollConceptAssignmentEntity> assignments = payrollConceptAssignmentRepository.findByPayrollId(payroll.getId());

        List<Map<String, Serializable>> sortedConceptMaps = assignments.stream()
            .sorted(Comparator.comparing((PayrollConceptAssignmentEntity assignment) -> assignment.getConcept().getCalculationPriority()))
            .map((PayrollConceptAssignmentEntity assignment) -> {
                Map<String, Serializable> conceptMap = new HashMap<>();
                conceptMap.put("code", assignment.getConcept().getCode());
                conceptMap.put("value", assignment.getValue());
                conceptMap.put("category", assignment.getConcept().getCategory().getCode());
                conceptMap.put("priority", assignment.getConcept().getCalculationPriority());
                return conceptMap;
            })
            .collect(Collectors.toList());
        log.info("Pre-loaded and sorted {} payroll concept assignments.", sortedConceptMaps.size());

        // 6. Save all data to JobExecutionContext for subsequent steps
        var executionContext = chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext();

        // Payroll and Period data
        executionContext.put("payrollId", payroll.getId());
        executionContext.put("periodStart", periodStart.toString());
        executionContext.put("periodEnd", periodEnd.toString());
        executionContext.put("subsidiaryId", payroll.getSubsidiary().getId());

        // Working days data
        executionContext.put("workingDays", workingDays.stream().map(LocalDate::toString).toList());
        executionContext.put("totalWorkingDays", totalWorkingDays);

        // Company configuration
        executionContext.put("overtimeRate", company.getOvertimeRate());
        executionContext.put("dailyNormalHours", company.getDailyNormalHours());
        executionContext.put("monthCalculationDays", company.getMonthCalculationDays());

        // Payroll configuration (as a list of maps to preserve all data and order)
        executionContext.put("payrollConfigurations", (Serializable) sortedConceptMaps);

        log.info("=== Step 0 completed successfully ===");

        return RepeatStatus.FINISHED;
    }
}
