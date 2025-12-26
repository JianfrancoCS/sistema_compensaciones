package com.agropay.core.payroll.batch.tasklet;

import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.payroll.service.PayslipPdfService;
import com.agropay.core.payroll.service.PayslipPdfStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tasklet para generar PDFs de boletas de pago y guardarlos en SQL Server
 * 
 * Este tasklet se ejecuta después de calcular todos los detalles de planilla.
 * Genera el PDF para cada empleado y lo guarda en SQL Server usando archivos internos, guardando la URL en la BD.
 * 
 * Flow:
 * 1. Obtiene todos los detalles de planilla calculados
 * 2. Para cada detalle:
 *    - Genera el PDF usando PayslipPdfService
 *    - Guarda el PDF en SQL Server usando PayslipPdfStorageService (archivos internos)
 *    - Guarda la URL en el PayrollDetailEntity
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratePayslipPdfsTasklet implements Tasklet {

    private final IPayrollRepository payrollRepository;
    private final IPayrollDetailRepository payrollDetailRepository;
    private final PayslipPdfService payslipPdfService;
    private final PayslipPdfStorageService payslipPdfStorageService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== Starting Step: Generate Payslip PDFs and Save to SQL Server ===");

        // 1. Try to get payrollId from JobExecutionContext (for payroll calculation job)
        Long payrollIdFromContext = (Long) chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext()
            .get("payrollId");

        PayrollEntity payroll;
        final Long payrollId;
        
        if (payrollIdFromContext != null) {
            // If payrollId is in context, use it (from payroll calculation job)
            payrollId = payrollIdFromContext;
            log.info("PayrollId from context: {}", payrollId);
            payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalStateException("Payroll not found with ID: " + payrollId));
        } else {
            // Otherwise, get payrollPublicId from JobParameters (for generate payslips job)
            String payrollPublicIdStr = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getJobParameters()
                .getString("payrollPublicId");
            
            if (payrollPublicIdStr == null) {
                throw new IllegalStateException("Neither payrollId nor payrollPublicId found in job context/parameters.");
            }
            
            log.info("PayrollPublicId from parameters: {}", payrollPublicIdStr);
            payroll = payrollRepository.findByPublicId(java.util.UUID.fromString(payrollPublicIdStr))
                .orElseThrow(() -> new IllegalStateException("Payroll not found with publicId: " + payrollPublicIdStr));
            payrollId = payroll.getId();
        }

        log.info("Generating PDFs for payroll: {} (Code: {})", payroll.getId(), payroll.getCode());

        // 3. Get all payroll details for this payroll
        List<PayrollDetailEntity> details = payrollDetailRepository.findByPayrollId(payrollId);
        log.info("Found {} payroll details to process", details.size());

        int successCount = 0;
        int errorCount = 0;

        // 4. Generate and upload PDF for each detail
        for (PayrollDetailEntity detail : details) {
            try {
                String employeeDocumentNumber = detail.getEmployee().getPersonDocumentNumber();
                log.debug("Processing PDF for employee: {}", employeeDocumentNumber);

                // Generar PDF
                byte[] pdfBytes = payslipPdfService.generatePayslipPdf(
                    payroll.getPublicId(),
                    employeeDocumentNumber
                );

                // Guardar en SQL Server usando archivos internos
                String pdfUrl = payslipPdfStorageService.uploadPayslipPdf(pdfBytes, detail);

                // Guardar URL en el detalle
                detail.setPayslipPdfUrl(pdfUrl);
                payrollDetailRepository.save(detail);

                successCount++;
                log.debug("PDF generated and saved successfully for employee: {}. URL: {}", 
                    employeeDocumentNumber, pdfUrl);

            } catch (Exception e) {
                errorCount++;
                log.error("Error generating PDF for employee: {}. Error: {}", 
                    detail.getEmployee().getPersonDocumentNumber(), e.getMessage(), e);
                // Continuar con el siguiente empleado aunque falle uno
            }
        }

        log.info("=== Step 3 completed. Success: {}, Errors: {} ===", successCount, errorCount);

        if (errorCount > 0) {
            log.warn("Some PDFs failed to generate. Check logs for details.");
        }

        return RepeatStatus.FINISHED;
    }
}

