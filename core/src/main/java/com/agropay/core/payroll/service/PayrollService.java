package com.agropay.core.payroll.service;

import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.persistence.ISubsidiaryRepository;
import com.agropay.core.organization.persistence.ICompanyRepository;
import com.agropay.core.payroll.domain.PayrollConceptAssignmentEntity;
import com.agropay.core.payroll.domain.PayrollConfigurationConceptEntity;
import com.agropay.core.payroll.domain.PayrollConfigurationEntity;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.domain.PayrollPeriodEntity;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import com.agropay.core.payroll.enums.PayrollState;
import com.agropay.core.payroll.mapper.PayrollMapper;
import com.agropay.core.payroll.model.payroll.CommandPayrollResponse;
import com.agropay.core.payroll.model.payroll.CreatePayrollRequest;
import com.agropay.core.payroll.model.payroll.PayrollListDTO;
import com.agropay.core.payroll.model.payroll.PayrollPageableRequest;
import com.agropay.core.payroll.model.payroll.PayrollSummaryDTO;
import com.agropay.core.payroll.persistence.IPayrollConceptAssignmentRepository;
import com.agropay.core.payroll.persistence.IPayrollConfigurationConceptRepository;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.payroll.persistence.IPayrollMasterConfigurationRepository;
import com.agropay.core.payroll.persistence.IPayrollPeriodRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agropay.core.organization.persistence.ICompanySubsidiarySignerRepository;
import com.agropay.core.organization.domain.CompanySubsidiarySignerEntity;
import com.agropay.core.payroll.persistence.specification.PayrollSpecifications;
import com.agropay.core.payroll.service.usecase.IPayrollService;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.utils.PagedResult;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.states.persistence.StateRepository;
import com.agropay.core.assignment.persistence.ITareoRepository;
import com.agropay.core.assignment.persistence.ITareoEmployeeRepository;
import com.agropay.core.assignment.persistence.IQrRollEmployeeRepository;
import com.agropay.core.assignment.persistence.IHarvestRecordRepository;
import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.payroll.domain.enums.CalendarEventTypeCode;
import com.agropay.core.payroll.persistence.IWorkCalendarRepository;
import java.time.DayOfWeek;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PayrollService implements IPayrollService {

    private final IPayrollRepository payrollRepository;
    private final IPayrollPeriodRepository payrollPeriodRepository;
    private final ISubsidiaryRepository subsidiaryRepository;
    private final StateRepository stateRepository;
    private final PayrollMapper payrollMapper;
    private final JobLauncher jobLauncher;
    private final Job payrollProcessingJob;
    @Qualifier("generatePayslipsJob")
    private final Job generatePayslipsJob;
    private final IPayrollMasterConfigurationRepository payrollConfigurationRepository;
    private final IPayrollConfigurationConceptRepository payrollConfigurationConceptRepository;
    private final IPayrollConceptAssignmentRepository payrollConceptAssignmentRepository;
    private final IPayrollDetailRepository payrollDetailRepository;
    private final ICompanyRepository companyRepository;
    private final ICompanySubsidiarySignerRepository companySubsidiarySignerRepository;
    private final ObjectMapper objectMapper;
    private final com.agropay.core.payroll.service.PayslipPdfService payslipPdfService;
    private final com.agropay.core.payroll.service.PayslipPdfStorageService payslipPdfStorageService;
    private final ITareoRepository tareoRepository;
    private final ITareoEmployeeRepository tareoEmployeeRepository;
    private final IQrRollEmployeeRepository qrRollEmployeeRepository;
    private final IHarvestRecordRepository harvestRecordRepository;
    private final IWorkCalendarRepository workCalendarRepository;
    private final IConceptRepository conceptRepository;

    @Override
    public CommandPayrollResponse createPayroll(CreatePayrollRequest request) {
        SubsidiaryEntity subsidiary = subsidiaryRepository.findByPublicId(request.subsidiaryPublicId())
            .orElseThrow(() -> new EntityNotFoundException("Subsidiary not found."));

        PayrollPeriodEntity period = payrollPeriodRepository.findByPublicId(request.payrollPeriodPublicId())
            .orElseThrow(() -> new EntityNotFoundException("Payroll Period not found."));

        PayrollConfigurationEntity activePayrollConfig = payrollConfigurationRepository.findTop1ByDeletedAtIsNullOrderByIdDesc()
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.configuration.not-found"
            ));

        StateEntity draftState = stateRepository.findByCodeAndDomainName(PayrollState.DRAFT.getCode(), PayrollEntity.TABLE_NAME)
            .orElseThrow(() -> new IllegalStateException("Draft state for payroll not found."));

        String subsidiaryCode = subsidiary.getName().substring(0, Math.min(subsidiary.getName().length(), 3)).toUpperCase();
        // Incluir period_number en el código para evitar duplicados cuando hay múltiples períodos en el mismo mes
        String payrollCode = String.format("PLAN-%s-%d-%02d", subsidiaryCode, period.getYear(), period.getMonth());
        if (period.getPeriodNumber() != null && period.getPeriodNumber() > 1) {
            payrollCode += "-" + period.getPeriodNumber();
        }
        
        // Verificar que no exista una planilla activa con el mismo código
        if (payrollRepository.findByCode(payrollCode).isPresent()) {
            throw new BusinessValidationException(
                "exception.payroll.code-already-exists",
                payrollCode
            );
        }

        // Validar que la subsidiaria tenga un responsable de firma con imagen asignado
        // Esto es necesario porque el servicio de PDF usa la imagen de firma
        validateSubsidiarySigner(subsidiary);

        // Calcular semanas ISO 8601
        Short weekStart = calculateIsoWeek(period.getPeriodStart());
        Short weekEnd = calculateIsoWeek(period.getPeriodEnd());

        // Calcular empleados y tareos que se van a procesar
        Long employeesToProcess = tareoRepository.countEmployeesWithTareosInPeriod(
            period.getPeriodStart(),
            period.getPeriodEnd(),
            subsidiary.getId()
        );
        Long tareosToProcess = tareoRepository.countTareosInPeriod(
            period.getPeriodStart(),
            period.getPeriodEnd(),
            subsidiary.getId()
        );
        
        log.info("Payroll pre-calculation for period {} to {}: {} employees, {} tareos", 
            period.getPeriodStart(), period.getPeriodEnd(), employeesToProcess, tareosToProcess);

        PayrollEntity payroll = PayrollEntity.builder()
            .publicId(UUID.randomUUID())
            .code(payrollCode)
            .subsidiary(subsidiary)
            .payrollConfiguration(activePayrollConfig)
            .year(period.getYear())
            .month(period.getMonth().shortValue())
            .periodStart(period.getPeriodStart())
            .periodEnd(period.getPeriodEnd())
            .weekStart(weekStart)
            .weekEnd(weekEnd)
            .paymentDate(null)
            .state(draftState)
            .totalEmployees(employeesToProcess != null ? employeesToProcess.intValue() : 0) // Establecer el total de empleados
            .build();

        payrollRepository.save(payroll);
        log.info("Created new payroll with code: {} - {} employees to process, {} tareos to process", 
            payrollCode, employeesToProcess, tareosToProcess);

        // Copy concepts from the active master configuration to the specific payroll's assignments
        List<PayrollConfigurationConceptEntity> masterConcepts = payrollConfigurationConceptRepository.findByPayrollConfigurationId(activePayrollConfig.getId());
        List<PayrollConceptAssignmentEntity> payrollAssignments = masterConcepts.stream()
            .map(masterConcept -> PayrollConceptAssignmentEntity.builder()
                .publicId(UUID.randomUUID())
                .payroll(payroll)
                .concept(masterConcept.getConcept())
                .value(masterConcept.getValue())
                .build())
            .collect(Collectors.toList());

        payrollConceptAssignmentRepository.saveAll(payrollAssignments);
        log.info("Copied {} concepts from master configuration to new payroll {}.", payrollAssignments.size(), payroll.getCode());

        CommandPayrollResponse response = new CommandPayrollResponse(
            payroll.getPublicId(),
            payroll.getCode(),
            payroll.getState().getName(),
            payroll.getTotalEmployees(),
            tareosToProcess,
            payroll.getCreatedAt(),
            payroll.getUpdatedAt()
        );
        
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandPayrollResponse launchPayrollCalculation(UUID payrollPublicId) {
        // Primero, ejecutar las validaciones y actualización del estado en una transacción
        PayrollEntity payroll = preparePayrollForCalculation(payrollPublicId);

        // Lanzar el job fuera de la transacción (Spring Batch requiere esto)
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("payrollPublicId", payroll.getPublicId().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            log.info("🚀 Lanzando job de procesamiento de planilla: Code={}, ID={}, Periodo={} a {}", 
                payroll.getCode(), payroll.getId(), payroll.getPeriodStart(), payroll.getPeriodEnd());
            
            jobLauncher.run(payrollProcessingJob, jobParameters);
            
            log.info("✅ Job de procesamiento lanzado exitosamente para planilla: {}", payroll.getCode());
        } catch (Exception e) {
            log.error("❌ ERROR al lanzar job de procesamiento de planilla: Code={}, Error={}", 
                payroll.getCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to launch payroll processing job", e);
        }

        // Recargar la entidad para obtener el estado actualizado
        payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        return payrollMapper.toCommandResponse(payroll);
    }

    /**
     * Prepara la planilla para el cálculo: valida y actualiza el estado.
     * Este método se ejecuta dentro de una transacción.
     */
    @Transactional
    private PayrollEntity preparePayrollForCalculation(UUID payrollPublicId) {
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        PayrollState currentState = PayrollState.fromCode(payroll.getState().getCode());
        if (!currentState.canLaunchCalculation()) {
            throw new BusinessValidationException(
                "exception.payroll.launch.invalid-state",
                currentState.getDisplayName()
            );
        }

        // Validar que la subsidiaria tenga un responsable de firma asignado
        validateSubsidiarySigner(payroll);

        StateEntity inProgressState = stateRepository.findByCodeAndDomainName(PayrollState.IN_PROGRESS.getCode(), PayrollEntity.TABLE_NAME)
            .orElseThrow(() -> new IllegalStateException("In Progress state for payroll not found."));

        payroll.setState(inProgressState);
        return payrollRepository.save(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<PayrollListDTO> listPayrolls(PayrollPageableRequest request) {
        Specification<PayrollEntity> spec = PayrollSpecifications.from(request);
        Page<PayrollEntity> payrollPage = payrollRepository.findAll(spec, request.toPageable());
        Page<PayrollListDTO> dtoPage = payrollPage.map(entity -> {
            PayrollListDTO dto = payrollMapper.toListDTO(entity);
            // Enriquecer con hasPayslips, tareos procesados y empleados procesados (progreso en tiempo real)
            Boolean hasPayslips = payrollDetailRepository.hasPayslips(entity.getId());
            Long processedTareos = payrollDetailRepository.countProcessedTareosByPayrollId(entity.getId());
            Long processedEmployees = payrollDetailRepository.countProcessedEmployeesByPayrollId(entity.getId());
            
            // totalEmployees en la entidad es el total estimado, pero en el DTO mostramos los procesados
            // para ver el progreso en tiempo real cuando se refresca la lista
            Integer employeesProcessed = processedEmployees != null ? processedEmployees.intValue() : 0;
            
            log.debug("Payroll {} - Total estimado: {}, Procesados: {}, Tareos procesados: {}", 
                entity.getCode(), entity.getTotalEmployees(), employeesProcessed, processedTareos);
            
            return new PayrollListDTO(
                dto.publicId(),
                dto.code(),
                dto.subsidiaryName(),
                dto.periodName(),
                dto.stateName(),
                employeesProcessed, // Mostrar empleados procesados (progreso en tiempo real)
                processedTareos != null ? processedTareos : 0L,
                hasPayslips != null ? hasPayslips : false,
                dto.createdAt(),
                dto.updatedAt()
            );
        });
        return new PagedResult<>(dtoPage);
    }

    @Override
    public void deletePayroll(UUID payrollPublicId) {
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        // Validate state - only DRAFT payrolls can be deleted
        PayrollState currentState = PayrollState.fromCode(payroll.getState().getCode());
        if (!currentState.isDeletable()) {
            throw new BusinessValidationException(
                "exception.payroll.delete.invalid-state",
                currentState.getDisplayName()
            );
        }

        // Validate that no employee details have been generated
        if (payroll.getDetails() != null && !payroll.getDetails().isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.delete.has-details",
                payroll.getCode(),
                String.valueOf(payroll.getDetails().size())
            );
        }

        // Soft delete the payroll
        payrollRepository.softDelete(payroll.getId(), "SYSTEM");
        log.info("Soft-deleted payroll: {}", payroll.getCode());
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandPayrollResponse generatePayslips(UUID payrollPublicId) {
        log.info("Generating payslips for payroll: {}", payrollPublicId);
        
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        PayrollState currentState = PayrollState.fromCode(payroll.getState().getCode());
        if (currentState != PayrollState.CALCULATED && currentState != PayrollState.APPROVED) {
            throw new BusinessValidationException(
                "exception.payroll.generate-payslips.invalid-state",
                currentState.getDisplayName()
            );
        }

        // Verificar que no haya boletas ya generadas
        if (payrollDetailRepository.hasPayslips(payroll.getId())) {
            throw new BusinessValidationException(
                "exception.payroll.generate-payslips.already-generated",
                payroll.getCode()
            );
        }

        // Lanzar el job de generación de boletas
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("payrollPublicId", payroll.getPublicId().toString())
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            log.info("🚀 Lanzando job de generación de boletas: Code={}, ID={}", 
                payroll.getCode(), payroll.getId());
            
            jobLauncher.run(generatePayslipsJob, jobParameters);
            
            log.info("✅ Job de generación de boletas lanzado exitosamente para planilla: {}", payroll.getCode());
        } catch (Exception e) {
            log.error("❌ ERROR al lanzar job de generación de boletas: Code={}, Error={}", 
                payroll.getCode(), e.getMessage(), e);
            throw new BusinessValidationException(
                "exception.payroll.generate-payslips.failed",
                payroll.getCode(),
                e.getMessage()
            );
        }

        // Recargar la entidad para obtener el estado actualizado
        payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        return payrollMapper.toCommandResponse(payroll);
    }

    @Override
    public CommandPayrollResponse cancelPayroll(UUID payrollPublicId) {
        log.info("Cancelling payroll: {}", payrollPublicId);
        
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        PayrollState currentState = PayrollState.fromCode(payroll.getState().getCode());
        
        // Solo se puede anular si está calculada o cerrada y no tiene boletas generadas
        if (currentState != PayrollState.CALCULATED && currentState != PayrollState.APPROVED) {
            throw new BusinessValidationException(
                "exception.payroll.cancel.invalid-state",
                currentState.getDisplayName()
            );
        }

        // Verificar que no haya boletas generadas
        if (payrollDetailRepository.hasPayslips(payroll.getId())) {
            throw new BusinessValidationException(
                "exception.payroll.cancel.has-payslips",
                payroll.getCode()
            );
        }

        StateEntity cancelledState = stateRepository.findByCodeAndDomainName(
            PayrollState.CANCELLED.getCode(), 
            PayrollEntity.TABLE_NAME
        ).orElseThrow(() -> new IllegalStateException("Cancelled state for payroll not found."));

        payroll.setState(cancelledState);
        payrollRepository.save(payroll);
        
        log.info("Payroll {} cancelled successfully", payroll.getCode());
        return payrollMapper.toCommandResponse(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollSummaryDTO getPayrollSummary(UUID payrollPublicId) {
        log.info("Fetching payroll summary for public ID: {}", payrollPublicId);
        
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        List<PayrollDetailEntity> details = payrollDetailRepository.findByPayrollId(payroll.getId());
        
        // Obtener totales - CALCULAR DESDE LOS DETALLES, NO USAR VALORES RAW DE LA PLANILLA
        Long totalEmployees = (long) details.size();
        BigDecimal totalIncome = details.stream()
            .map(d -> d.getTotalIncome() != null ? d.getTotalIncome() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeductions = details.stream()
            .map(d -> d.getTotalDeductions() != null ? d.getTotalDeductions() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Calcular aportaciones del empleador desde los detalles
        BigDecimal totalEmployerContributions = details.stream()
            .map(d -> d.getTotalEmployerContributions() != null ? d.getTotalEmployerContributions() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = totalIncome.subtract(totalDeductions);

        // Procesar conceptos agrupados por categoría
        Map<String, Map<String, BigDecimal>> conceptTotalsByCategory = new HashMap<>();
        
        for (PayrollDetailEntity detail : details) {
            Map<String, Object> calculatedConcepts = parseCalculatedConcepts(detail.getCalculatedConcepts());
            
            for (Map.Entry<String, Object> entry : calculatedConcepts.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> conceptData = (Map<String, Object>) entry.getValue();
                String category = (String) conceptData.get("category");
                String conceptCode = entry.getKey();
                BigDecimal amount = getBigDecimal(conceptData.get("amount"));
                
                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                
                conceptTotalsByCategory
                    .computeIfAbsent(category, k -> new HashMap<>())
                    .merge(conceptCode, amount, BigDecimal::add);
            }
        }
        
        // Log para depuración: mostrar conceptos de jubilación agrupados
        Map<String, BigDecimal> retirementConcepts = conceptTotalsByCategory.getOrDefault(
            com.agropay.core.payroll.domain.enums.ConceptCategoryCode.RETIREMENT.getCode(), 
            new HashMap<>()
        );
        log.info("📊 Conceptos de jubilación encontrados en el resumen: {}", retirementConcepts);
        log.info("📊 Total de detalles procesados: {}", details.size());
        log.info("📊 Conceptos por categoría - INCOME: {}, DEDUCTION: {}, RETIREMENT: {}, EMPLOYER_CONTRIBUTION: {}", 
            conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME.getCode(), new HashMap<>()).size(),
            conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.DEDUCTION.getCode(), new HashMap<>()).size(),
            retirementConcepts.size(),
            conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYER_CONTRIBUTION.getCode(), new HashMap<>()).size()
        );

        // Calcular totales específicos por categoría usando el enum
        // ESSALUD está en EMPLOYER_CONTRIBUTION
        BigDecimal totalHealth = conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYER_CONTRIBUTION.getCode(), new HashMap<>())
            .entrySet().stream()
            .filter(entry -> entry.getKey().equals("ESSALUD") || entry.getKey().contains("HEALTH"))
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // AFP y ONP están en RETIREMENT - SUMAR TODOS LOS CONCEPTOS DE JUBILACIÓN
        BigDecimal totalRetirement = conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.RETIREMENT.getCode(), new HashMap<>())
            .entrySet().stream()
            .filter(entry -> entry.getKey().contains("AFP") || entry.getKey().equals("ONP"))
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Remuneración: Sumar BASIC_SALARY y OVERTIME desde los conceptos calculados
        BigDecimal totalRemuneration = conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME.getCode(), new HashMap<>())
            .entrySet().stream()
            .filter(entry -> {
                String key = entry.getKey();
                return key.equals("BASIC_SALARY") || key.equals("OVERTIME") || 
                       key.contains("SALARY") || key.contains("BASIC") || key.contains("REMUNERATION");
            })
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Bonos están en INCOME
        BigDecimal totalBonus = conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME.getCode(), new HashMap<>())
            .entrySet().stream()
            .filter(entry -> {
                String key = entry.getKey();
                return key.contains("BONUS") || key.contains("BONO") || 
                       key.equals("ATTENDANCE_BONUS") || key.equals("PRODUCTIVITY_BONUS");
            })
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convertir a listas de ConceptSummary usando el enum
        List<PayrollSummaryDTO.ConceptSummary> incomeConcepts = buildConceptSummaryList(
            conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME.getCode(), new HashMap<>()), 
            com.agropay.core.payroll.domain.enums.ConceptCategoryCode.INCOME.getCode()
        );
        List<PayrollSummaryDTO.ConceptSummary> deductionConcepts = buildConceptSummaryList(
            combineMaps(
                conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.DEDUCTION.getCode(), new HashMap<>()),
                conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.RETIREMENT.getCode(), new HashMap<>()),
                conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYEE_CONTRIBUTION.getCode(), new HashMap<>())
            ), com.agropay.core.payroll.domain.enums.ConceptCategoryCode.DEDUCTION.getCode()
        );
        List<PayrollSummaryDTO.ConceptSummary> employerContributionConcepts = buildConceptSummaryList(
            conceptTotalsByCategory.getOrDefault(com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYER_CONTRIBUTION.getCode(), new HashMap<>()), 
            com.agropay.core.payroll.domain.enums.ConceptCategoryCode.EMPLOYER_CONTRIBUTION.getCode()
        );

        // Construir período label
        String periodLabel = String.format("%s - %s",
            payroll.getPeriodStart().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            payroll.getPeriodEnd().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        return new PayrollSummaryDTO(
            payroll.getPublicId(),
            payroll.getCode(),
            payroll.getSubsidiary().getName(),
            payroll.getSubsidiary().getPublicId(),
            Integer.valueOf(payroll.getYear()),
            payroll.getMonth(),
            periodLabel,
            totalEmployees,
            totalIncome,
            totalDeductions,
            totalEmployerContributions,
            totalNet,
            totalHealth.setScale(2, RoundingMode.HALF_UP),
            totalRetirement.setScale(2, RoundingMode.HALF_UP),
            totalRemuneration.setScale(2, RoundingMode.HALF_UP),
            totalBonus.setScale(2, RoundingMode.HALF_UP),
            incomeConcepts,
            deductionConcepts,
            employerContributionConcepts
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.agropay.core.assignment.model.tareo.TareoListDTO> getProcessedTareos(UUID payrollPublicId) {
        log.info("Fetching processed tareos for payroll public ID: {}", payrollPublicId);
        
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        List<com.agropay.core.assignment.domain.TareoEntity> tareos = payrollDetailRepository.findProcessedTareosByPayrollId(payroll.getId());
        
        return tareos.stream()
            .map(tareo -> {
                long employeeCount = tareoRepository.countTareoEmployeesByTareoId(tareo.getId());
                String loteName = tareo.getLote() != null ? tareo.getLote().getName() : null;
                String subsidiaryName = tareo.getSubsidiary() != null ? tareo.getSubsidiary().getName() : null;
                return new com.agropay.core.assignment.model.tareo.TareoListDTO(
                    tareo.getPublicId(),
                    tareo.getLabor() != null ? tareo.getLabor().getName() : null,
                    loteName,
                    subsidiaryName,
                    employeeCount,
                    true, 
                    tareo.getCreatedAt()
                );
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> parseCalculatedConcepts(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Error parsing calculated concepts JSON", e);
            return new HashMap<>();
        }
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private List<PayrollSummaryDTO.ConceptSummary> buildConceptSummaryList(
            Map<String, BigDecimal> conceptTotals, String category) {
        // Mostrar TODOS los conceptos por separado, ordenados por monto descendente
        // Esto permite que cualquier concepto configurado en la planilla aparezca dinámicamente
        return conceptTotals.entrySet().stream()
            .filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0) // Solo mostrar conceptos con valor > 0
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .map(entry -> {
                String conceptCode = entry.getKey();
                String conceptName = getConceptNameForSummary(conceptCode);
                log.debug("Agregando concepto al resumen: {} ({}) = {}", conceptCode, conceptName, entry.getValue());
                return new PayrollSummaryDTO.ConceptSummary(
                    conceptCode,
                    conceptName,
                    entry.getValue().setScale(2, RoundingMode.HALF_UP),
                    category
                );
            })
            .collect(Collectors.toList());
    }

    private String getConceptNameForSummary(String conceptCode) {
        // Para el resumen, usar el nombre completo del concepto desde la BD
        // Esto permite que cualquier concepto configurado en la planilla se muestre dinámicamente
        // Buscar el concepto en la BD para obtener su nombre completo
        return conceptRepository.findAll().stream()
            .filter(c -> c.getCode().equals(conceptCode) && c.getDeletedAt() == null)
            .findFirst()
            .map(ConceptEntity::getName)
            .orElseGet(() -> {
                // Si no se encuentra en la BD, intentar obtener el nombre del enum
                try {
                    ConceptCode.valueOf(conceptCode); // Validar que existe en el enum
                    return ConceptCode.getPayslipDisplayName(conceptCode);
                } catch (IllegalArgumentException e) {
                    // Si no es un ConceptCode válido, usar el código formateado como nombre
                    return formatConceptCodeAsName(conceptCode);
                }
            });
    }
    
    /**
     * Formatea un código de concepto como nombre legible
     * Ejemplo: "ATTENDANCE_BONUS" -> "Attendance Bonus"
     */
    private String formatConceptCodeAsName(String conceptCode) {
        // Reemplazar guiones bajos con espacios y capitalizar cada palabra
        String[] words = conceptCode.replace("_", " ").toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase())
                      .append(words[i].substring(1));
            }
        }
        return result.toString();
    }

    @SafeVarargs
    private Map<String, BigDecimal> combineMaps(Map<String, BigDecimal>... maps) {
        Map<String, BigDecimal> combined = new HashMap<>();
        for (Map<String, BigDecimal> map : maps) {
            map.forEach((key, value) -> combined.merge(key, value, BigDecimal::add));
        }
        return combined;
    }

    /**
     * Valida que la subsidiaria de la planilla tenga un responsable de firma asignado
     * con imagen de firma. Busca primero por subsidiaria específica, luego a nivel de empresa.
     * 
     * @param payroll Planilla a validar
     * @throws BusinessValidationException Si no se encuentra un responsable asignado o si no tiene imagen de firma
     */
    private void validateSubsidiarySigner(PayrollEntity payroll) {
        SubsidiaryEntity subsidiary = payroll.getSubsidiary();
        if (subsidiary == null) {
            throw new BusinessValidationException(
                "exception.payroll.launch.missing-subsidiary"
            );
        }

        // Obtener la empresa principal
        var companyOpt = companyRepository.getPrimaryCompany();
        if (companyOpt.isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.launch.company-not-found"
            );
        }

        var company = companyOpt.get();
        Short subsidiaryId = subsidiary.getId();

        // Buscar responsable para la subsidiaria específica
        Optional<CompanySubsidiarySignerEntity> signerOpt = companySubsidiarySignerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiaryId);

        // Si no encuentra para la subsidiaria, buscar a nivel de empresa
        if (signerOpt.isEmpty()) {
            signerOpt = companySubsidiarySignerRepository.findLatestByCompany(company.getId());
        }

        if (signerOpt.isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.launch.missing-signer",
                subsidiary.getName()
            );
        }

        CompanySubsidiarySignerEntity signer = signerOpt.get();
        
        // Validar que el responsable tenga imagen de firma
        // El campo signatureImageUrl siempre contiene la URL de descarga (archivo interno o legacy)
        if (signer.getSignatureImageUrl() == null || signer.getSignatureImageUrl().trim().isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.launch.missing-signature-image",
                subsidiary.getName()
            );
        }

        log.info("Responsable de firma validado para subsidiaria {}: {} (con imagen de firma: {})", 
            subsidiary.getName(), 
            signer.getResponsibleEmployee().getPersonDocumentNumber(),
            signer.getSignatureImageUrl());
    }
    
    /**
     * Valida que la subsidiaria tenga un responsable de firma con imagen asignado.
     * Versión sobrecargada que acepta la subsidiaria directamente.
     * Usado en createPayroll.
     * 
     * @param subsidiary Subsidiaria a validar
     * @throws BusinessValidationException Si no se encuentra un responsable asignado o si no tiene imagen de firma
     */
    private void validateSubsidiarySigner(SubsidiaryEntity subsidiary) {
        if (subsidiary == null) {
            throw new BusinessValidationException(
                "exception.payroll.launch.missing-subsidiary"
            );
        }

        // Obtener la empresa principal
        var companyOpt = companyRepository.getPrimaryCompany();
        if (companyOpt.isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.launch.company-not-found"
            );
        }

        var company = companyOpt.get();
        Short subsidiaryId = subsidiary.getId();

        // Buscar responsable para la subsidiaria específica
        Optional<CompanySubsidiarySignerEntity> signerOpt = companySubsidiarySignerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiaryId);

        // Si no encuentra para la subsidiaria, buscar a nivel de empresa
        if (signerOpt.isEmpty()) {
            signerOpt = companySubsidiarySignerRepository.findLatestByCompany(company.getId());
        }

        if (signerOpt.isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.create.missing-signer",
                subsidiary.getName()
            );
        }

        CompanySubsidiarySignerEntity signer = signerOpt.get();
        
        // Validar que el responsable tenga imagen de firma
        // El campo signatureImageUrl siempre contiene la URL de descarga (archivo interno o legacy)
        if (signer.getSignatureImageUrl() == null || signer.getSignatureImageUrl().trim().isEmpty()) {
            throw new BusinessValidationException(
                "exception.payroll.create.missing-signature-image",
                subsidiary.getName()
            );
        }

        log.info("Responsable de firma validado para subsidiaria {}: {} (con imagen de firma: {})", 
            subsidiary.getName(), 
            signer.getResponsibleEmployee().getPersonDocumentNumber(),
            signer.getSignatureImageUrl());
    }

    /**
     * Calcula el número de semana ISO 8601 para una fecha
     * ISO 8601: La semana comienza el lunes y la primera semana del año es la que contiene el 4 de enero
     */
    private Short calculateIsoWeek(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
        return (short) weekNumber;
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.agropay.core.payroll.model.payroll.PayrollEmployeeListDTO> getPayrollEmployees(
            UUID payrollPublicId, UUID laborPublicId, String employeeDocumentNumber) {
        log.info("Fetching payroll employees for public ID: {}, labor: {}, employee: {}", 
            payrollPublicId, laborPublicId, employeeDocumentNumber);
        
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        String employeeFilter = employeeDocumentNumber != null && !employeeDocumentNumber.trim().isEmpty()
            ? "%" + employeeDocumentNumber.trim() + "%"
            : null;

        List<PayrollDetailEntity> details = payrollDetailRepository.findByPayrollIdWithFilters(
            payroll.getId(),
            laborPublicId,
            employeeFilter,
            payroll.getPeriodStart(),
            payroll.getPeriodEnd()
        );

        return details.stream()
            .map(this::toPayrollEmployeeListDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO getPayrollEmployeeDetail(
            UUID payrollPublicId, String employeeDocumentNumber) {
        log.info("Fetching payroll employee detail for public ID: {}, employee: {}", 
            payrollPublicId, employeeDocumentNumber);
        
        PayrollEntity payroll = payrollRepository.findByPublicId(payrollPublicId)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.not-found",
                payrollPublicId.toString()
            ));

        PayrollDetailEntity detail = payrollDetailRepository
            .findByPayrollIdAndEmployeeDocumentNumber(payroll.getId(), employeeDocumentNumber)
            .orElseThrow(() -> new BusinessValidationException(
                "exception.payroll.employee.not-found",
                employeeDocumentNumber
            ));

        return toPayrollEmployeeDetailDTO(detail);
    }

    private com.agropay.core.payroll.model.payroll.PayrollEmployeeListDTO toPayrollEmployeeListDTO(PayrollDetailEntity detail) {
        var employee = detail.getEmployee();
        var person = employee.getPerson();
        String fullName = person != null 
            ? String.format("%s %s %s", 
                person.getNames(),
                person.getPaternalLastname(),
                person.getMaternalLastname() != null ? person.getMaternalLastname() : "")
            : employee.getPersonDocumentNumber();
        
        String positionName = employee.getPosition() != null 
            ? employee.getPosition().getName() 
            : "Sin cargo";

        return new com.agropay.core.payroll.model.payroll.PayrollEmployeeListDTO(
            detail.getPublicId(),
            employee.getPersonDocumentNumber(),
            fullName.trim(),
            positionName,
            detail.getTotalIncome(),
            detail.getTotalDeductions(),
            detail.getNetToPay(),
            detail.getDaysWorked()
        );
    }

    private com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO toPayrollEmployeeDetailDTO(PayrollDetailEntity detail) {
        var employee = detail.getEmployee();
        var person = employee.getPerson();
        String fullName = person != null 
            ? String.format("%s %s %s", 
                person.getNames(),
                person.getPaternalLastname(),
                person.getMaternalLastname() != null ? person.getMaternalLastname() : "")
            : employee.getPersonDocumentNumber();
        
        String positionName = employee.getPosition() != null 
            ? employee.getPosition().getName() 
            : "Sin cargo";

        Map<String, Object> calculatedConcepts = parseCalculatedConcepts(detail.getCalculatedConcepts());
        
        // Generar todos los días del período con información completa
        PayrollEntity payroll = detail.getPayroll();
        List<com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO.DailyWorkDetail> dailyDetails = 
            buildAllDaysDetail(payroll.getPeriodStart(), payroll.getPeriodEnd(), detail);

        return new com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO(
            detail.getPublicId(),
            employee.getPersonDocumentNumber(),
            fullName.trim(),
            positionName,
            detail.getTotalIncome(),
            detail.getTotalDeductions(),
            detail.getTotalEmployerContributions(),
            detail.getNetToPay(),
            detail.getDaysWorked(),
            detail.getNormalHours(),
            detail.getOvertimeHours25(),
            detail.getOvertimeHours35(),
            detail.getOvertimeHours100(),
            detail.getNightHours(),
            calculatedConcepts,
            dailyDetails
        );
    }

    /**
     * Construye la lista de todos los días del período, incluyendo los que no fueron trabajados.
     * Para cada día trabajado, incluye información de productividad con valor numérico y unidad.
     */
    private List<com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO.DailyWorkDetail> buildAllDaysDetail(
            LocalDate periodStart, LocalDate periodEnd, PayrollDetailEntity detail) {
        
        // Parsear los días trabajados del JSON
        Map<LocalDate, Map<String, Object>> workedDaysMap = parseDailyDetailToMap(detail.getDailyDetail());
        
        // Obtener información de productividad desde los tareos
        Map<LocalDate, ProductivityInfo> productivityMap = getProductivityInfo(
            detail.getEmployee().getPersonDocumentNumber(), 
            periodStart, 
            periodEnd
        );
        
        // Generar todos los días del período
        List<com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO.DailyWorkDetail> allDays = new ArrayList<>();
        
        LocalDate currentDate = periodStart;
        while (!currentDate.isAfter(periodEnd)) {
            Map<String, Object> workedDayData = workedDaysMap.get(currentDate);
            ProductivityInfo productivity = productivityMap.get(currentDate);
            boolean worked = workedDayData != null;
            
            BigDecimal hours = worked && workedDayData.containsKey("hours") 
                ? getBigDecimal(workedDayData.get("hours")) 
                : BigDecimal.ZERO;
            BigDecimal nightHours = worked && workedDayData.containsKey("nightHours")
                ? getBigDecimal(workedDayData.get("nightHours"))
                : BigDecimal.ZERO;
            BigDecimal performancePercentage = worked && workedDayData.containsKey("performance")
                ? getBigDecimal(workedDayData.get("performance"))
                : BigDecimal.ZERO;
            
            Long productivityValue = productivity != null ? productivity.harvestCount() : null;
            String productivityUnit = productivity != null ? productivity.unitName() : null;
            
            boolean isHoliday = isHoliday(currentDate);
            boolean isNonWorkingDay = isNonWorkingDay(currentDate);
            
            allDays.add(new com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO.DailyWorkDetail(
                currentDate.toString(),
                currentDate.getDayOfWeek().name(),
                hours,
                nightHours,
                performancePercentage,
                productivityValue,
                productivityUnit,
                isHoliday,
                isNonWorkingDay,
                worked
            ));
            
            currentDate = currentDate.plusDays(1);
        }
        
        return allDays;
    }

    /**
     * Obtiene información de productividad (harvestCount y unidad) por día desde los tareos
     */
    private Map<LocalDate, ProductivityInfo> getProductivityInfo(String employeeDocumentNumber, LocalDate periodStart, LocalDate periodEnd) {
        Map<LocalDate, ProductivityInfo> productivityMap = new HashMap<>();
        
        // Obtener tareos del empleado en el período
        var tareos = tareoEmployeeRepository.findByEmployeeAndPeriod(employeeDocumentNumber, periodStart, periodEnd);
        var qrRolls = qrRollEmployeeRepository.findByEmployeeAndPeriod(employeeDocumentNumber, periodStart, periodEnd);
        
        Map<LocalDate, com.agropay.core.assignment.domain.TareoEmployeeEntity> tareosByDate = tareos.stream()
            .collect(Collectors.toMap(t -> t.getCreatedAt().toLocalDate(), t -> t, (t1, t2) -> t2));
        Map<LocalDate, com.agropay.core.assignment.domain.QrRollEmployeeEntity> qrRollsByDate = qrRolls.stream()
            .collect(Collectors.toMap(qr -> qr.getAssignedDate(), qr -> qr, (qr1, qr2) -> qr2));
        
        for (Map.Entry<LocalDate, com.agropay.core.assignment.domain.QrRollEmployeeEntity> entry : qrRollsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            var tareo = tareosByDate.get(date);
            
            if (tareo != null && tareo.getTareo().getLabor() != null) {
                var labor = tareo.getTareo().getLabor();
                
                // Solo para labores de destajo
                if (Boolean.TRUE.equals(labor.getIsPiecework()) && labor.getLaborUnit() != null) {
                    Long harvestCount = harvestRecordRepository.countByQrRollId(entry.getValue().getQrRoll().getId());
                    String unitName = labor.getLaborUnit().getName();
                    
                    productivityMap.put(date, new ProductivityInfo(harvestCount, unitName));
                }
            }
        }
        
        return productivityMap;
    }

    private record ProductivityInfo(Long harvestCount, String unitName) {}

    private Map<LocalDate, Map<String, Object>> parseDailyDetailToMap(String json) {
        Map<LocalDate, Map<String, Object>> result = new HashMap<>();
        try {
            if (json == null || json.isEmpty()) {
                return result;
            }
            List<Map<String, Object>> dailyDetailsList = objectMapper.readValue(
                json, 
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            for (Map<String, Object> day : dailyDetailsList) {
                String dateStr = (String) day.get("date");
                if (dateStr != null) {
                    LocalDate date = LocalDate.parse(dateStr);
                    result.put(date, day);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing daily detail JSON to map", e);
        }
        return result;
    }

    /**
     * Verifica si una fecha es domingo
     */
    private boolean isSunday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * Verifica si una fecha es feriado (tiene evento de tipo HOLIDAY en el calendario)
     * NO asume que todos los domingos son feriados
     */
    private boolean isHoliday(LocalDate date) {
        Optional<WorkCalendarEntity> calendarDay = workCalendarRepository.findByDate(date);
        if (calendarDay.isEmpty()) return false;
        return calendarDay.get().getEvents().stream()
            .anyMatch(event -> event.getEventType().getCode().equals(CalendarEventTypeCode.HOLIDAY.name()));
    }

    /**
     * Verifica si una fecha es día no laborable (domingo o feriado)
     */
    private boolean isNonWorkingDay(LocalDate date) {
        Optional<WorkCalendarEntity> calendarDay = workCalendarRepository.findByDate(date);
        if (calendarDay.isEmpty()) {
            // Si no existe en BD, asumir que solo domingos son no laborables
            return date.getDayOfWeek() == DayOfWeek.SUNDAY;
        }
        return !calendarDay.get().getIsWorkingDay();
    }
}
