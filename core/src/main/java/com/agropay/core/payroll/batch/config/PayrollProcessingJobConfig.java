package com.agropay.core.payroll.batch.config;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.payroll.batch.processor.EmployeePayrollProcessor;
import com.agropay.core.payroll.batch.tasklet.CalculateWorkingDaysTasklet;
import com.agropay.core.payroll.batch.tasklet.GeneratePayslipPdfsTasklet;
import com.agropay.core.payroll.batch.tasklet.UpdatePayrollTotalsTasklet;
import com.agropay.core.payroll.batch.writer.PayrollDetailWriter;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

    /**
     * Configuración del Job de procesamiento de planillas
     *
     * Flow del Job:
     * 1. Step 0: CalculateWorkingDaysTasklet - Prepara los datos del período y calcula días laborables
     * 2. Step 1: ProcessEmployeesStep - Lee empleados, calcula conceptos, guarda detalles (chunk-based)
     * 3. Step 2: UpdatePayrollTotalsTasklet - Actualiza totales de la planilla
     * 
     * NOTA: La generación de boletas (PDFs) NO está incluida en este job.
     * Las boletas se generan por separado cuando el usuario presiona el botón "Generar Boletas".
     */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PayrollProcessingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationContext applicationContext;

    // Tasklets
    private final CalculateWorkingDaysTasklet calculateWorkingDaysTasklet;
    private final UpdatePayrollTotalsTasklet updatePayrollTotalsTasklet;
    private final GeneratePayslipPdfsTasklet generatePayslipPdfsTasklet;

    // Chunk components - NO inyectar el reader aquí porque es @StepScope
    private final EmployeePayrollProcessor employeePayrollProcessor;
    private final PayrollDetailWriter payrollDetailWriter;

    /**
     * Step 0: Calcula días laborables y prepara contexto del job
     */
    @Bean
    public Step calculateWorkingDaysStep() {
        return new StepBuilder("calculateWorkingDaysStep", jobRepository)
            .tasklet(calculateWorkingDaysTasklet, transactionManager)
            .build();
    }

    /**
     * Step 1: Procesa empleados en chunks (lee, calcula, escribe)
     * 
     * IMPORTANTE: El reader es @StepScope, por lo que Spring Batch lo creará dinámicamente
     * cuando el step se ejecute. Usamos el nombre del bean como string para que Spring Batch
     * lo resuelva correctamente y llame a open() antes de leer.
     */
    @Bean
    public Step processEmployeesStep() {
        // Usar ApplicationContext para obtener el bean @StepScope dinámicamente
        return new StepBuilder("processEmployeesStep", jobRepository)
            .<EmployeeEntity, PayrollDetailEntity>chunk(10, transactionManager)
            .reader((org.springframework.batch.item.ItemReader<EmployeeEntity>) applicationContext.getBean("employeeItemReader"))
            .processor(employeePayrollProcessor)
            .writer(payrollDetailWriter)
            .listener(new ItemReadListener<EmployeeEntity>() {
                private int readCount = 0;
                
                @Override
                public void beforeRead() {
                    // No action needed
                }
                
                @Override
                public void afterRead(EmployeeEntity employee) {
                    readCount++;
                    log.info("📖 EmployeeReader: Leyendo empleado #{} - DNI: {}, Nombre: {}", 
                        readCount,
                        employee.getPersonDocumentNumber(),
                        employee.getPerson() != null ? employee.getPerson().getNames() : "N/A");
                }
                
                @Override
                public void onReadError(Exception ex) {
                    log.error("❌ Error al leer empleado: {}", ex.getMessage(), ex);
                }
            })
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("=== INICIANDO Step 1: processEmployeesStep ===");
                    log.info("Job Execution ID: {}", stepExecution.getJobExecutionId());
                    log.info("Step Execution ID: {}", stepExecution.getId());
                }

                @Override
                public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("=== FINALIZANDO Step 1: processEmployeesStep ===");
                    log.info("Read Count: {}", stepExecution.getReadCount());
                    log.info("Write Count: {}", stepExecution.getWriteCount());
                    log.info("Filter Count: {}", stepExecution.getFilterCount());
                    log.info("Process Skip Count: {}", stepExecution.getProcessSkipCount());
                    log.info("Read Skip Count: {}", stepExecution.getReadSkipCount());
                    log.info("Write Skip Count: {}", stepExecution.getWriteSkipCount());
                    log.info("Commit Count: {}", stepExecution.getCommitCount());
                    log.info("Rollback Count: {}", stepExecution.getRollbackCount());
                    log.info("Exit Status: {}", stepExecution.getExitStatus());
                    
                    if (stepExecution.getReadCount() == 0) {
                        log.error("⚠️ ADVERTENCIA: No se leyeron empleados. Verificar query del EmployeeReader.");
                        log.error("   Verificar: 1) Query del EmployeeReader, 2) Tareos en el período, 3) Subsidiaria correcta");
                    } else {
                        log.info("✅ EmployeeReader: Fin de lectura. Total empleados leídos: {}", stepExecution.getReadCount());
                    }
                    if (stepExecution.getWriteCount() == 0) {
                        log.error("⚠️ ADVERTENCIA: No se escribieron detalles. Verificar procesamiento.");
                    }
                    
                    return stepExecution.getExitStatus();
                }
            })
            .build();
    }

    /**
     * Step 2: Actualiza totales de la planilla
     */
    @Bean
    public Step updatePayrollTotalsStep() {
        return new StepBuilder("updatePayrollTotalsStep", jobRepository)
            .tasklet(updatePayrollTotalsTasklet, transactionManager)
            .build();
    }

    /**
     * Step 3: Genera PDFs de boletas y los sube a Cloudinary
     * Este step se usa en el job separado generatePayslipsJob
     */
    @Bean
    public Step generatePayslipPdfsStep() {
        return new StepBuilder("generatePayslipPdfsStep", jobRepository)
            .tasklet(generatePayslipPdfsTasklet, transactionManager)
            .build();
    }

    /**
     * Job separado para generar boletas de pago (PDFs)
     * 
     * Este job se ejecuta cuando el usuario presiona el botón "Generar Boletas"
     * en una planilla ya calculada.
     * 
     * Flow:
     * 1. Step: GeneratePayslipPdfsStep - Genera PDFs y los sube a Cloudinary
     *
     * JobParameter requerido: payrollPublicId (UUID)
     */
    @Bean
    public Job generatePayslipsJob() {
        return new JobBuilder("generatePayslipsJob", jobRepository)
            .listener(new org.springframework.batch.core.JobExecutionListener() {
                @Override
                public void beforeJob(org.springframework.batch.core.JobExecution jobExecution) {
                    log.info("═══════════════════════════════════════════════════════════");
                    log.info("🎯 INICIANDO JOB: generatePayslipsJob");
                    log.info("   Job Execution ID: {}", jobExecution.getId());
                    log.info("   Job Parameters: {}", jobExecution.getJobParameters());
                    log.info("═══════════════════════════════════════════════════════════");
                }

                @Override
                public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
                    log.info("═══════════════════════════════════════════════════════════");
                    log.info("🏁 FINALIZANDO JOB: generatePayslipsJob");
                    log.info("   Job Execution ID: {}", jobExecution.getId());
                    log.info("   Exit Status: {}", jobExecution.getExitStatus());
                    log.info("   Start Time: {}", jobExecution.getStartTime());
                    log.info("   End Time: {}", jobExecution.getEndTime());
                    if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
                        long duration = java.time.Duration.between(
                            jobExecution.getStartTime().toInstant(java.time.ZoneOffset.UTC),
                            jobExecution.getEndTime().toInstant(java.time.ZoneOffset.UTC)
                        ).getSeconds();
                        log.info("   Duración: {} segundos", duration);
                    }
                    
                    if (jobExecution.getExitStatus().getExitCode().equals("FAILED")) {
                        log.error("❌ JOB FALLÓ - Revisar logs anteriores para detalles");
                        if (jobExecution.getFailureExceptions() != null && !jobExecution.getFailureExceptions().isEmpty()) {
                            jobExecution.getFailureExceptions().forEach(ex -> {
                                log.error("   Excepción: {}", ex.getMessage(), ex);
                            });
                        }
                    } else {
                        log.info("✅ JOB COMPLETADO EXITOSAMENTE");
                    }
                    log.info("═══════════════════════════════════════════════════════════");
                }
            })
            .start(generatePayslipPdfsStep())
            .build();
    }

    /**
     * Job principal de procesamiento de planillas
     *
     * Flow:
     * 1. Step 0: CalculateWorkingDaysStep - Prepara contexto y calcula días laborables
     * 2. Step 1: ProcessEmployeesStep - Procesa empleados en chunks (lee, calcula, escribe)
     * 3. Step 2: UpdatePayrollTotalsStep - Actualiza totales de la planilla
     *
     * NOTA: La generación de boletas (PDFs) NO está incluida en este job.
     * Las boletas se generan por separado mediante el método generatePayslips() en PayrollService.
     *
     * JobParameter requerido: payrollPublicId (UUID)
     */
    @Bean
    public Job payrollProcessingJob() {
        return new JobBuilder("payrollProcessingJob", jobRepository)
            .listener(new org.springframework.batch.core.JobExecutionListener() {
                @Override
                public void beforeJob(org.springframework.batch.core.JobExecution jobExecution) {
                    log.info("═══════════════════════════════════════════════════════════");
                    log.info("🎯 INICIANDO JOB: payrollProcessingJob");
                    log.info("   Job Execution ID: {}", jobExecution.getId());
                    log.info("   Job Parameters: {}", jobExecution.getJobParameters());
                    log.info("═══════════════════════════════════════════════════════════");
                }

                @Override
                public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
                    log.info("═══════════════════════════════════════════════════════════");
                    log.info("🏁 FINALIZANDO JOB: payrollProcessingJob");
                    log.info("   Job Execution ID: {}", jobExecution.getId());
                    log.info("   Exit Status: {}", jobExecution.getExitStatus());
                    log.info("   Start Time: {}", jobExecution.getStartTime());
                    log.info("   End Time: {}", jobExecution.getEndTime());
                    if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
                        long duration = java.time.Duration.between(
                            jobExecution.getStartTime().toInstant(java.time.ZoneOffset.UTC),
                            jobExecution.getEndTime().toInstant(java.time.ZoneOffset.UTC)
                        ).getSeconds();
                        log.info("   Duración: {} segundos", duration);
                    }
                    
                    if (jobExecution.getExitStatus().getExitCode().equals("FAILED")) {
                        log.error("❌ JOB FALLÓ - Revisar logs anteriores para detalles");
                        if (jobExecution.getFailureExceptions() != null && !jobExecution.getFailureExceptions().isEmpty()) {
                            jobExecution.getFailureExceptions().forEach(ex -> {
                                log.error("   Excepción: {}", ex.getMessage(), ex);
                            });
                        }
                    } else {
                        log.info("✅ JOB COMPLETADO EXITOSAMENTE");
                    }
                    log.info("═══════════════════════════════════════════════════════════");
                }
            })
            .start(calculateWorkingDaysStep())
            .next(processEmployeesStep())
            .next(updatePayrollTotalsStep())
            .build();
    }
}
