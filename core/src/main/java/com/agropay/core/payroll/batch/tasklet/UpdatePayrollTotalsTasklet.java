package com.agropay.core.payroll.batch.tasklet;

import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.model.payroll.PayrollTotalsDTO;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.states.persistence.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Tasklet to update the payroll totals and set its final state after processing all employees.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePayrollTotalsTasklet implements Tasklet {

    private final IPayrollRepository payrollRepository;
    private final IPayrollDetailRepository payrollDetailRepository;
    private final StateRepository stateRepository;

    private static final String PAYROLL_DOMAIN = "tbl_payrolls";
    private static final String CALCULATED_STATE_CODE = "PAYROLL_CALCULATED";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== Starting Step 2: Update Payroll Totals and State ===");

        // 1. Get payrollId from the JobExecutionContext
        Long payrollId = (Long) chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext()
            .get("payrollId");

        if (payrollId == null) {
            throw new IllegalStateException("payrollId not found in JobExecutionContext.");
        }
        log.info("PayrollId: {}", payrollId);

        // 2. Fetch the PayrollEntity
        PayrollEntity payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalStateException("Payroll not found with ID: " + payrollId));

        // 3. Get aggregated totals directly from the database
        PayrollTotalsDTO totals = payrollDetailRepository.getPayrollTotals(payrollId);

        // 4. Calculate net total
        BigDecimal totalIncome = totals.totalIncome() != null ? totals.totalIncome() : BigDecimal.ZERO;
        BigDecimal totalDeductions = totals.totalDeductions() != null ? totals.totalDeductions() : BigDecimal.ZERO;
        BigDecimal totalNet = totalIncome.subtract(totalDeductions);

        log.info("📊 Totales calculados:");
        log.info("   - Empleados procesados: {}", totals.totalEmployees());
        log.info("   - Total Ingresos: {}", totalIncome);
        log.info("   - Total Descuentos: {}", totalDeductions);
        log.info("   - Total Neto: {}", totalNet);

        // 5. Get the new state for the payroll
        StateEntity calculatedState = stateRepository.findByCodeAndDomainName(CALCULATED_STATE_CODE, PAYROLL_DOMAIN)
            .orElseThrow(() -> new IllegalStateException("State '" + CALCULATED_STATE_CODE + "' not found for domain '" + PAYROLL_DOMAIN + "'."));
        log.info("Transitioning payroll to state: {}", calculatedState.getName());

        // 6. Update the PayrollEntity
        payroll.setTotalEmployees((int) totals.totalEmployees());
        payroll.setTotalIncome(totalIncome);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setTotalNet(totalNet);
        payroll.setState(calculatedState);

        // 7. Save the updated PayrollEntity
        payrollRepository.save(payroll);

        log.info("=== Step 2 completed successfully ===");

        return RepeatStatus.FINISHED;
    }
}
