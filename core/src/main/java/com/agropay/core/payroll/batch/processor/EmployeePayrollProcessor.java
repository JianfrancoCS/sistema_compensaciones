package com.agropay.core.payroll.batch.processor;

import com.agropay.core.assignment.domain.QrRollEmployeeEntity;
import com.agropay.core.assignment.domain.TareoEmployeeEntity;
import com.agropay.core.assignment.persistence.IHarvestRecordRepository;
import com.agropay.core.assignment.persistence.IQrRollEmployeeRepository;
import com.agropay.core.assignment.persistence.ITareoEmployeeRepository;
import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.hiring.persistence.IContractPositionSalaryRepository;
import com.agropay.core.hiring.persistence.IContractRepository;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.persistence.IPersonRepository;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.domain.calculator.ConceptCalculatorFactory;
import com.agropay.core.payroll.domain.calculator.EmployeePayrollContext;
import com.agropay.core.payroll.domain.calculator.WorkCalendarDayInfo;
import com.agropay.core.payroll.domain.enums.CalendarEventTypeCode;
import com.agropay.core.payroll.domain.enums.ConceptCode;
import com.agropay.core.payroll.persistence.IConceptRepository;
import com.agropay.core.payroll.persistence.IWorkCalendarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class EmployeePayrollProcessor implements ItemProcessor<EmployeeEntity, PayrollDetailEntity> {

    private final IWorkCalendarRepository workCalendarRepository;
    private final ITareoEmployeeRepository tareoEmployeeRepository;
    private final IHarvestRecordRepository harvestRecordRepository;
    private final IQrRollEmployeeRepository qrRollEmployeeRepository;
    private final IPersonRepository personRepository;
    private final ConceptCalculatorFactory calculatorFactory;
    private final IConceptRepository conceptRepository;
    private final IContractRepository contractRepository;
    private final IContractPositionSalaryRepository contractPositionSalaryRepository;
    private final ObjectMapper objectMapper;

    @Value("#{jobExecutionContext['periodStart']}")
    private String periodStartStr;

    @Value("#{jobExecutionContext['periodEnd']}")
    private String periodEndStr;

    @Value("#{jobExecutionContext['workingDays']}")
    private List<String> workingDaysStr;

    @Value("#{jobExecutionContext['totalWorkingDays']}")
    private Integer totalWorkingDays;

    @Value("#{jobExecutionContext['overtimeRate']}")
    private BigDecimal overtimeRate;

    @Value("#{jobExecutionContext['dailyNormalHours']}")
    private BigDecimal dailyNormalHours;

    @Value("#{jobExecutionContext['monthCalculationDays']}")
    private Integer monthCalculationDays;

    @Value("#{jobExecutionContext['payrollConfigurations']}")
    private List<Map<String, Serializable>> payrollConfigurations;

    @Override
    public PayrollDetailEntity process(EmployeeEntity employee) throws Exception {
        log.info("=== EmployeePayrollProcessor: Procesando empleado: {} ===", employee.getPersonDocumentNumber());
        log.info("Empleado - DNI: {}, Nombre: {}, Subsidiaria: {}", 
            employee.getPersonDocumentNumber(),
            employee.getPerson() != null ? employee.getPerson().getNames() : "N/A",
            employee.getSubsidiary() != null ? employee.getSubsidiary().getName() : "N/A");

        LocalDate periodStart = LocalDate.parse(periodStartStr);
        LocalDate periodEnd = LocalDate.parse(periodEndStr);
        List<LocalDate> workingDays = workingDaysStr.stream().map(LocalDate::parse).toList();

        BigDecimal basicSalary = getBasicSalary(employee);
        Map<LocalDate, DayHoursDetail> hoursPerDay = calculateHoursPerDay(employee, periodStart, periodEnd);
        HoursBreakdown hoursBreakdown = categorizeHours(hoursPerDay);
        
        // Calcular días trabajados: contar días con tareos registrados
        // Estos son los días que realmente trabajó el empleado (tiene tareos)
        int daysWorked = hoursPerDay.size();
        
        // Crear set de días trabajados para verificación rápida (usado en dominical)
        Set<LocalDate> workedDaysSet = new HashSet<>(hoursPerDay.keySet());
        
        // Obtener información del calendario para los días trabajados
        Map<LocalDate, WorkCalendarDayInfo> calendarInfo = buildCalendarInfo(workedDaysSet, periodStart, periodEnd);
        
        // Solo calcular productividad si el empleado tiene labores de destajo
        // Empleados administrativos (labores sin destajo) no tienen productividad
        Map<LocalDate, BigDecimal> productivityPerDay = calculateProductivityPerDay(employee, periodStart, periodEnd);
        BigDecimal productivityScore = calculateProductivityScore(employee, periodStart, periodEnd, productivityPerDay);
        
        // Calcular información de destajo por día (basePrice, minTaskRequirement, productivityCount)
        // Necesario para calcular el pago del excedente de destajo
        Map<LocalDate, com.agropay.core.payroll.domain.calculator.PieceworkDayInfo> pieceworkInfoPerDay = 
            calculatePieceworkInfoPerDay(employee, periodStart, periodEnd);
        
        Integer numberOfDependents = countDependents(employee);

        // Filtrar configuraciones con valores null antes de crear el mapa
        // Collectors.toMap() no acepta valores null
        Map<ConceptCode, BigDecimal> configuredConceptsMap = payrollConfigurations.stream()
            .filter(map -> map.get("value") != null) // Filtrar valores null
            .collect(Collectors.toMap(
                map -> ConceptCode.valueOf((String) map.get("code")),
                map -> (BigDecimal) map.get("value"),
                (v1, v2) -> v1
            ));

        EmployeePayrollContext context = EmployeePayrollContext.builder()
            .employeeDocumentNumber(employee.getPersonDocumentNumber())
            .basicSalary(basicSalary)
            .retirementConceptId(employee.getRetirementConceptId())
            .healthInsuranceConceptId(employee.getHealthInsuranceConceptId())
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .workingDays(workingDays)
            .totalWorkingDays(totalWorkingDays)
            .daysWorked(daysWorked)
            .workedDays(workedDaysSet)
            .calendarInfo(calendarInfo)
            .overtimeRate(overtimeRate)
            .dailyNormalHours(dailyNormalHours)
            .monthCalculationDays(monthCalculationDays)
            .configuredConcepts(configuredConceptsMap)
            .numberOfDependents(numberOfDependents)
            .productivityScore(productivityScore) // Mantener para compatibilidad
            .productivityPerDay(productivityPerDay) // Agregar productividad por día
            .pieceworkInfoPerDay(pieceworkInfoPerDay) // Información de destajo por día (para cálculo de excedente)
            .normalHours(hoursBreakdown.normalHours)
            .overtimeHours25(hoursBreakdown.overtimeHours25)
            .overtimeHours100(hoursBreakdown.overtimeHours100)
            .totalHours(hoursBreakdown.totalHours)
            .build();

        // Obtener el código del concepto de jubilación del empleado
        String employeeRetirementConceptCode = null;
        if (employee.getRetirementConceptId() != null) {
            Optional<ConceptEntity> retirementConcept = conceptRepository.findById(employee.getRetirementConceptId());
            if (retirementConcept.isPresent()) {
                employeeRetirementConceptCode = retirementConcept.get().getCode();
            }
        }

        Map<String, Object> calculatedConcepts = new LinkedHashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalEmployerContributions = BigDecimal.ZERO;

        for (Map<String, Serializable> config : payrollConfigurations) {
            ConceptCode conceptCode = ConceptCode.valueOf((String) config.get("code"));
            String categoryCode = (String) config.get("category");
            
            // Filtrar conceptos de jubilación: solo calcular el que corresponde al empleado
            if (com.agropay.core.payroll.domain.enums.ConceptCategoryCode.RETIREMENT.getCode().equals(categoryCode)) {
                if (employeeRetirementConceptCode == null || !conceptCode.name().equals(employeeRetirementConceptCode)) {
                    // No calcular este concepto de jubilación si no corresponde al empleado
                    continue;
                }
            }
            
            if (!calculatorFactory.hasCalculator(conceptCode)) {
                log.warn("No calculator for concept: {}", conceptCode);
                continue;
            }

            BigDecimal amount = calculatorFactory.getCalculator(conceptCode).calculate(context);
            Map<String, Object> conceptData = new HashMap<>();
            conceptData.put("amount", amount);
            conceptData.put("category", categoryCode);
            if (config.get("value") != null) {
                conceptData.put("value", config.get("value"));
            }
            calculatedConcepts.put(conceptCode.name(), conceptData);

            com.agropay.core.payroll.domain.enums.ConceptCategoryCode category = 
                com.agropay.core.payroll.domain.enums.ConceptCategoryCode.fromCode(categoryCode);
            
            switch (category) {
                case INCOME -> totalIncome = totalIncome.add(amount);
                case DEDUCTION, RETIREMENT, EMPLOYEE_CONTRIBUTION -> totalDeductions = totalDeductions.add(amount);
                case EMPLOYER_CONTRIBUTION -> totalEmployerContributions = totalEmployerContributions.add(amount);
            }

            context.setTotalIncome(totalIncome);
            context.setTotalDeductions(totalDeductions);
        }

        BigDecimal netToPay = totalIncome.subtract(totalDeductions);
        String dailyDetailJson = buildDailyDetailJson(hoursPerDay, productivityPerDay);

        PayrollDetailEntity detail = PayrollDetailEntity.builder()
            .publicId(UUID.randomUUID())
            .employee(employee)
            .calculatedConcepts(objectMapper.writeValueAsString(calculatedConcepts))
            .dailyDetail(dailyDetailJson)
            .totalIncome(totalIncome)
            .totalDeductions(totalDeductions)
            .totalEmployerContributions(totalEmployerContributions)
            .netToPay(netToPay)
            .daysWorked((short) hoursPerDay.size())
            .normalHours(hoursBreakdown.normalHours)
            .overtimeHours25(hoursBreakdown.overtimeHours25)
            .overtimeHours35(hoursBreakdown.overtimeHours35)
            .overtimeHours100(hoursBreakdown.overtimeHours100)
            .nightHours(hoursBreakdown.nightHours)
            .totalHours(hoursBreakdown.totalHours)
            .build();

        log.info("Employee {} processed - Net: {}", employee.getPersonDocumentNumber(), netToPay);
        return detail;
    }

    /**
     * Construye el mapa de información del calendario para los días trabajados
     */
    private Map<LocalDate, WorkCalendarDayInfo> buildCalendarInfo(Set<LocalDate> workedDays, LocalDate periodStart, LocalDate periodEnd) {
        Map<LocalDate, WorkCalendarDayInfo> calendarInfoMap = new HashMap<>();
        
        // Obtener todos los días del calendario en el período
        List<com.agropay.core.payroll.domain.WorkCalendarEntity> calendarDays = 
            workCalendarRepository.findAllBetweenWithEvents(periodStart, periodEnd);
        
        // Crear mapa rápido por fecha
        Map<LocalDate, com.agropay.core.payroll.domain.WorkCalendarEntity> calendarMap = calendarDays.stream()
            .collect(Collectors.toMap(
                com.agropay.core.payroll.domain.WorkCalendarEntity::getDate,
                day -> day,
                (v1, v2) -> v1
            ));
        
        // Para cada día trabajado, crear la información del calendario
        for (LocalDate date : workedDays) {
            com.agropay.core.payroll.domain.WorkCalendarEntity calendarDay = calendarMap.get(date);
            boolean isHoliday = calendarDay != null && calendarDay.isHoliday();
            boolean isSunday = date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
            boolean isWorkingDay = calendarDay != null ? calendarDay.isWorkingDay() : !isSunday;
            
            WorkCalendarDayInfo info = WorkCalendarDayInfo.builder()
                .isWorkingDay(isWorkingDay)
                .isHoliday(isHoliday)
                .isSunday(isSunday)
                .dayOfWeek(date.getDayOfWeek())
                .build();
            
            calendarInfoMap.put(date, info);
        }
        
        return calendarInfoMap;
    }

    /**
     * Obtiene el salario mensual del empleado.
     * Prioridad:
     * 1. Salario especial en tabla N a N (contract_position_salaries) - si existe contrato activo
     * 2. Salario de la posición (positions.salary)
     * 
     * @param employee Empleado
     * @return Salario mensual
     */
    private BigDecimal getBasicSalary(EmployeeEntity employee) {
        // Buscar contrato activo del empleado
        Optional<ContractEntity> contractOpt = contractRepository.findByPersonDocumentNumber(
            employee.getPersonDocumentNumber()
        );
        
        // Si hay contrato, buscar salario especial en tabla N a N
        if (contractOpt.isPresent()) {
            ContractEntity contract = contractOpt.get();
            Optional<com.agropay.core.hiring.domain.ContractPositionSalaryEntity> specialSalaryOpt = 
                contractPositionSalaryRepository.findActiveByContractIdAndPositionId(
                    contract.getEntityId(), // Usar getEntityId() que devuelve Long directamente
                    employee.getPosition().getId()
                );
            
            if (specialSalaryOpt.isPresent()) {
                log.debug("Usando salario especial de contract_position_salaries para empleado {}", 
                    employee.getPersonDocumentNumber());
                return specialSalaryOpt.get().getSalary();
            }
        }
        
        // Si no hay salario especial, usar el de la posición
        BigDecimal positionSalary = employee.getPosition().getSalary();
        if (positionSalary == null) {
            log.warn("No salary configured for position {} for employee {}", 
                employee.getPosition().getName(), 
                employee.getPersonDocumentNumber());
            return BigDecimal.ZERO;
        }
        
        return positionSalary;
    }

    private Map<LocalDate, DayHoursDetail> calculateHoursPerDay(EmployeeEntity employee, LocalDate periodStart, LocalDate periodEnd) {
        List<TareoEmployeeEntity> tareos = tareoEmployeeRepository.findByEmployeeAndPeriod(employee.getPersonDocumentNumber(), periodStart, periodEnd);
        Map<LocalDate, DayHoursDetail> result = new HashMap<>();
        
        for (TareoEmployeeEntity tareo : tareos) {
            if (tareo.getPaidHours() == null) continue;
            
            LocalDate date = tareo.getCreatedAt().toLocalDate();
            DayHoursDetail detail = result.getOrDefault(date, new DayHoursDetail(BigDecimal.ZERO, BigDecimal.ZERO));
            
            BigDecimal totalHours = detail.totalHours().add(tareo.getPaidHours());
            BigDecimal nightHours = detail.nightHours();
            
            // Calcular horas nocturnas (22:00 - 06:00)
            if (tareo.getStartTime() != null && tareo.getEndTime() != null) {
                nightHours = nightHours.add(calculateNightHours(tareo.getStartTime(), tareo.getEndTime(), tareo.getPaidHours()));
            }
            
            result.put(date, new DayHoursDetail(totalHours, nightHours));
        }
        
        return result;
    }
    
    /**
     * Calcula las horas nocturnas trabajadas (22:00 - 06:00)
     * Rango nocturno: desde las 22:00 hasta las 06:00 del día siguiente
     */
    private BigDecimal calculateNightHours(LocalTime startTime, LocalTime endTime, BigDecimal totalHours) {
        LocalTime nightStart = LocalTime.of(22, 0);
        LocalTime nightEnd = LocalTime.of(6, 0);
        
        // Si el trabajo cruza la medianoche (endTime < startTime)
        boolean crossesMidnight = endTime.isBefore(startTime) || endTime.equals(startTime);
        
        if (crossesMidnight) {
            // Trabajo que cruza medianoche: calcular horas nocturnas en ambos días
            BigDecimal nightHoursBeforeMidnight = calculateNightHoursInRange(startTime, LocalTime.MAX, nightStart, nightEnd);
            BigDecimal nightHoursAfterMidnight = calculateNightHoursInRange(LocalTime.MIN, endTime, nightStart, nightEnd);
            return nightHoursBeforeMidnight.add(nightHoursAfterMidnight);
        }
        
        // Trabajo en el mismo día
        return calculateNightHoursInRange(startTime, endTime, nightStart, nightEnd);
    }
    
    /**
     * Calcula horas nocturnas en un rango específico
     */
    private BigDecimal calculateNightHoursInRange(LocalTime start, LocalTime end, LocalTime nightStart, LocalTime nightEnd) {
        // Si el rango está completamente fuera del horario nocturno
        if ((end.isBefore(nightStart) || end.equals(nightStart)) && 
            (start.isAfter(nightEnd) || start.equals(nightEnd)) && 
            !start.isBefore(nightStart)) {
            return BigDecimal.ZERO;
        }
        
        // Si empieza antes de las 22:00 y termina después de las 06:00
        if (start.isBefore(nightStart) && end.isAfter(nightEnd)) {
            // Horas nocturnas = 8 horas (22:00 a 06:00)
            return BigDecimal.valueOf(8);
        }
        
        // Si está completamente dentro del rango nocturno (22:00 - 06:00 del día siguiente)
        if ((start.isAfter(nightStart) || start.equals(nightStart)) && 
            (end.isBefore(nightEnd) || end.equals(nightEnd))) {
            long minutes = java.time.Duration.between(start, end).toMinutes();
            return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }
        
        // Si empieza en horario nocturno pero termina después
        if (start.isAfter(nightStart) || start.equals(nightStart)) {
            if (end.isAfter(nightEnd) && end.isBefore(nightStart)) {
                // Termina después de las 06:00 pero antes de las 22:00
                long minutes = java.time.Duration.between(start, nightEnd).toMinutes();
                return BigDecimal.valueOf(Math.max(0, minutes)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
        }
        
        // Si termina en horario nocturno pero empieza antes
        if (end.isBefore(nightEnd) || end.equals(nightEnd)) {
            if (start.isBefore(nightStart)) {
                // Empieza antes de las 22:00
                long minutes = java.time.Duration.between(nightStart, end).toMinutes();
                // Si end < nightStart, significa que es del día siguiente
                if (end.isBefore(nightStart)) {
                    minutes = java.time.Duration.between(LocalTime.MIN, end).toMinutes() + 
                             java.time.Duration.between(nightStart, LocalTime.MAX).toMinutes() + 1;
                }
                return BigDecimal.valueOf(Math.max(0, minutes)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
        }
        
        return BigDecimal.ZERO;
    }

    private HoursBreakdown categorizeHours(Map<LocalDate, DayHoursDetail> hoursPerDay) {
        BigDecimal normalHours = BigDecimal.ZERO;
        BigDecimal overtimeHours25 = BigDecimal.ZERO;
        BigDecimal overtimeHours35 = BigDecimal.ZERO;
        BigDecimal overtimeHours100 = BigDecimal.ZERO;
        BigDecimal nightHours = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, DayHoursDetail> entry : hoursPerDay.entrySet()) {
            LocalDate date = entry.getKey();
            DayHoursDetail detail = entry.getValue();
            BigDecimal hours = detail.totalHours();
            nightHours = nightHours.add(detail.nightHours());
            
            if (isSundayOrHoliday(date)) {
                // Domingos y feriados: 100% de recargo
                overtimeHours100 = overtimeHours100.add(hours);
            } else {
                if (hours.compareTo(dailyNormalHours) <= 0) {
                    normalHours = normalHours.add(hours);
                } else {
                    // Horas normales
                    normalHours = normalHours.add(dailyNormalHours);
                    BigDecimal extraHours = hours.subtract(dailyNormalHours);
                    
                    // Primeras 2 horas extras: 25%
                    // Siguientes horas extras: 35%
                    // Máximo 2 horas al 25%
                    BigDecimal hours25 = extraHours.min(BigDecimal.valueOf(2));
                    BigDecimal hours35 = extraHours.subtract(hours25);
                    
                    overtimeHours25 = overtimeHours25.add(hours25);
                    if (hours35.compareTo(BigDecimal.ZERO) > 0) {
                        overtimeHours35 = overtimeHours35.add(hours35);
                    }
                }
            }
        }
        
        BigDecimal totalHours = normalHours.add(overtimeHours25).add(overtimeHours35).add(overtimeHours100);
        return new HoursBreakdown(
            normalHours.setScale(2, RoundingMode.HALF_UP),
            overtimeHours25.setScale(2, RoundingMode.HALF_UP),
            overtimeHours35.setScale(2, RoundingMode.HALF_UP),
            overtimeHours100.setScale(2, RoundingMode.HALF_UP),
            nightHours.setScale(2, RoundingMode.HALF_UP),
            totalHours.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private boolean isSundayOrHoliday(LocalDate date) {
        if (date.getDayOfWeek().getValue() == 7) return true;
        Optional<WorkCalendarEntity> calendarDay = workCalendarRepository.findByDate(date);
        if (calendarDay.isEmpty()) return false;
        // A day is a holiday if it's marked as non-working and has an event of type HOLIDAY
        return !calendarDay.get().getIsWorkingDay() && calendarDay.get().getEvents().stream()
            .anyMatch(event -> event.getEventType().getCode().equals(CalendarEventTypeCode.HOLIDAY.name()));
    }

    /**
     * Calcula la productividad por día para empleados con labores de destajo
     * 
     * IMPORTANTE: Solo se calcula productividad si:
     * 1. La labor es de destajo (isPiecework = true)
     * 2. La labor tiene minTaskRequirement configurado
     * 3. El empleado tiene QR rolls asignados
     * 
     * Empleados administrativos (labores sin destajo) no tienen productividad
     * y retornarán un mapa vacío, lo cual es correcto.
     */
    private Map<LocalDate, BigDecimal> calculateProductivityPerDay(EmployeeEntity employee, LocalDate periodStart, LocalDate periodEnd) {
        List<TareoEmployeeEntity> tareos = tareoEmployeeRepository.findByEmployeeAndPeriod(employee.getPersonDocumentNumber(), periodStart, periodEnd);
        List<QrRollEmployeeEntity> qrRollAssignments = qrRollEmployeeRepository.findByEmployeeAndPeriod(employee.getPersonDocumentNumber(), periodStart, periodEnd);
        
        Map<LocalDate, BigDecimal> productivityMap = new HashMap<>();
        Map<LocalDate, TareoEmployeeEntity> tareosByDate = tareos.stream()
            .collect(Collectors.toMap(t -> t.getCreatedAt().toLocalDate(), t -> t, (t1, t2) -> t2));
        Map<LocalDate, QrRollEmployeeEntity> qrRollsByDate = qrRollAssignments.stream()
            .collect(Collectors.toMap(QrRollEmployeeEntity::getAssignedDate, qr -> qr, (qr1, qr2) -> qr2));

        for (Map.Entry<LocalDate, QrRollEmployeeEntity> entry : qrRollsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            TareoEmployeeEntity tareo = tareosByDate.get(date);
            
            // Validar que exista el tareo y la labor
            if (tareo == null || tareo.getTareo().getLabor() == null) {
                continue; // No agregar al mapa si no hay tareo
            }

            var labor = tareo.getTareo().getLabor();
            
            // SOLO calcular productividad si la labor es de DESTAJO
            // Empleados administrativos (isPiecework = false) NO tienen productividad
            if (!Boolean.TRUE.equals(labor.getIsPiecework())) {
                log.debug("Labor '{}' no es de destajo, omitiendo cálculo de productividad para empleado {} en fecha {}", 
                    labor.getName(), employee.getPersonDocumentNumber(), date);
                continue; // No agregar al mapa si no es de destajo
            }

            // Validar que la labor de destajo tenga minTaskRequirement
            if (labor.getMinTaskRequirement() == null || labor.getMinTaskRequirement().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Labor de destajo '{}' no tiene minTaskRequirement configurado. Omitiendo productividad para empleado {} en fecha {}", 
                    labor.getName(), employee.getPersonDocumentNumber(), date);
                continue;
            }

            // Usar el campo productivity de tareo_employees si está disponible (ya calculado al cerrar el tareo)
            // Si no está disponible, calcular desde harvest_records (compatibilidad hacia atrás)
            BigDecimal minTaskRequirement = labor.getMinTaskRequirement();
            Integer productivityCount = tareo.getProductivity();
            
            if (productivityCount != null) {
                // Usar el valor ya calculado y guardado en tareo_employees.productivity
                BigDecimal productivity = BigDecimal.valueOf(productivityCount)
                    .divide(minTaskRequirement, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                
                productivityMap.put(date, productivity);
                log.debug("Productividad obtenida de tareo_employees para empleado {} en fecha {}: {}% ({} / {})", 
                    employee.getPersonDocumentNumber(), date, productivity, productivityCount, minTaskRequirement);
            } else {
                // Fallback: calcular desde harvest_records (para tareos antiguos sin productivity calculado)
                Long harvestCount = harvestRecordRepository.countByQrRollId(entry.getValue().getQrRoll().getId());
                
                BigDecimal productivity = BigDecimal.valueOf(harvestCount)
                    .divide(minTaskRequirement, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                
                productivityMap.put(date, productivity);
                log.debug("Productividad calculada desde harvest_records para empleado {} en fecha {}: {}% ({} / {})", 
                    employee.getPersonDocumentNumber(), date, productivity, harvestCount, minTaskRequirement);
            }
        }
        
        return productivityMap;
    }

    /**
     * Calcula la información de destajo por día (basePrice, minTaskRequirement, productivityCount)
     * Necesario para calcular el pago del excedente de destajo
     * 
     * IMPORTANTE: Solo se calcula para labores de destajo (isPiecework = true)
     * 
     * @param employee Empleado
     * @param periodStart Inicio del período
     * @param periodEnd Fin del período
     * @return Mapa de información de destajo por día
     */
    private Map<LocalDate, com.agropay.core.payroll.domain.calculator.PieceworkDayInfo> calculatePieceworkInfoPerDay(
            EmployeeEntity employee, LocalDate periodStart, LocalDate periodEnd) {
        List<TareoEmployeeEntity> tareos = tareoEmployeeRepository.findByEmployeeAndPeriod(employee.getPersonDocumentNumber(), periodStart, periodEnd);
        List<QrRollEmployeeEntity> qrRollAssignments = qrRollEmployeeRepository.findByEmployeeAndPeriod(employee.getPersonDocumentNumber(), periodStart, periodEnd);
        
        Map<LocalDate, com.agropay.core.payroll.domain.calculator.PieceworkDayInfo> pieceworkInfoMap = new HashMap<>();
        Map<LocalDate, TareoEmployeeEntity> tareosByDate = tareos.stream()
            .collect(Collectors.toMap(t -> t.getCreatedAt().toLocalDate(), t -> t, (t1, t2) -> t2));
        Map<LocalDate, QrRollEmployeeEntity> qrRollsByDate = qrRollAssignments.stream()
            .collect(Collectors.toMap(QrRollEmployeeEntity::getAssignedDate, qr -> qr, (qr1, qr2) -> qr2));

        for (Map.Entry<LocalDate, QrRollEmployeeEntity> entry : qrRollsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            TareoEmployeeEntity tareo = tareosByDate.get(date);
            
            // Validar que exista el tareo y la labor
            if (tareo == null || tareo.getTareo().getLabor() == null) {
                continue;
            }

            var labor = tareo.getTareo().getLabor();
            
            // SOLO calcular información de destajo si la labor es de DESTAJO
            if (!Boolean.TRUE.equals(labor.getIsPiecework())) {
                continue; // No agregar al mapa si no es de destajo
            }

            // Validar que la labor de destajo tenga minTaskRequirement y basePrice
            if (labor.getMinTaskRequirement() == null || labor.getMinTaskRequirement().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Labor de destajo '{}' no tiene minTaskRequirement configurado. Omitiendo información de destajo para empleado {} en fecha {}", 
                    labor.getName(), employee.getPersonDocumentNumber(), date);
                continue;
            }
            
            if (labor.getBasePrice() == null || labor.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Labor de destajo '{}' no tiene basePrice configurado. Omitiendo información de destajo para empleado {} en fecha {}", 
                    labor.getName(), employee.getPersonDocumentNumber(), date);
                continue;
            }

            // Obtener productividad del día
            Integer productivityCount = tareo.getProductivity();
            
            if (productivityCount == null) {
                // Fallback: calcular desde harvest_records si no hay productivity en tareo_employees
                Long harvestCount = harvestRecordRepository.countByQrRollId(entry.getValue().getQrRoll().getId());
                productivityCount = harvestCount != null ? harvestCount.intValue() : 0;
            }

            // Crear PieceworkDayInfo con la información de la labor
            com.agropay.core.payroll.domain.calculator.PieceworkDayInfo dayInfo = 
                new com.agropay.core.payroll.domain.calculator.PieceworkDayInfo(
                    date,
                    labor.getMinTaskRequirement(),
                    labor.getBasePrice(),
                    productivityCount
                );

            pieceworkInfoMap.put(date, dayInfo);
            log.debug("Información de destajo para empleado {} en fecha {}: Mínimo: {}, Base Price: {}, Productividad: {}", 
                employee.getPersonDocumentNumber(), date, labor.getMinTaskRequirement(), labor.getBasePrice(), productivityCount);
        }
        
        return pieceworkInfoMap;
    }

    /**
     * Calcula el score de productividad para el período
     * 
     * IMPORTANTE:
     * - Si el empleado NO tiene labores de destajo → retorna null (no aplica)
     * - Si el empleado tiene labores de destajo pero no cumplió todos los días → retorna 0
     * - Si el empleado tiene labores de destajo y cumplió todos los días → retorna 100
     * 
     * @param employee Empleado
     * @param periodStart Inicio del período
     * @param periodEnd Fin del período
     * @param productivityPerDay Mapa de productividad por día (ya calculado)
     * @return Score de productividad (100 si cumplió todos los días, 0 si no, null si no aplica)
     */
    private BigDecimal calculateProductivityScore(EmployeeEntity employee, LocalDate periodStart, LocalDate periodEnd, Map<LocalDate, BigDecimal> productivityPerDay) {
        // Si no hay productividad calculada (empleado administrativo o sin labores de destajo)
        // Retornar null para indicar que no aplica
        if (productivityPerDay.isEmpty()) {
            log.debug("Empleado {} no tiene productividad calculada (probablemente labores administrativas)", 
                employee.getPersonDocumentNumber());
            return null; // null indica que no aplica (no es de destajo)
        }
        
        // Si hay productividad, verificar que cumplió el mínimo todos los días
        // Si algún día no cumple el mínimo (100%), retornar 0
        for (Map.Entry<LocalDate, BigDecimal> entry : productivityPerDay.entrySet()) {
            BigDecimal productivity = entry.getValue();
            if (productivity.compareTo(BigDecimal.valueOf(100)) < 0) {
                log.debug("Empleado {} no cumplió productividad en fecha {}: {}%", 
                    employee.getPersonDocumentNumber(), entry.getKey(), productivity);
                return BigDecimal.ZERO;
            }
        }
        
        // Si cumplió todos los días, retornar 100
        log.debug("Empleado {} cumplió productividad todos los días: 100%", employee.getPersonDocumentNumber());
        return new BigDecimal("100");
    }

    private Integer countDependents(EmployeeEntity employee) {
        return personRepository.countByPersonParentDocumentNumber(employee.getPersonDocumentNumber()).intValue();
    }

    private String buildDailyDetailJson(Map<LocalDate, DayHoursDetail> hoursPerDay, Map<LocalDate, BigDecimal> productivityPerDay) throws Exception {
        List<Map<String, Object>> dailyDetails = new ArrayList<>();
        for (Map.Entry<LocalDate, DayHoursDetail> entry : hoursPerDay.entrySet()) {
            LocalDate date = entry.getKey();
            DayHoursDetail detail = entry.getValue();
            
            Map<String, Object> dayDetail = new HashMap<>();
            dayDetail.put("date", date.toString());
            dayDetail.put("dayOfWeek", date.getDayOfWeek().name());
            dayDetail.put("hours", detail.totalHours());
            dayDetail.put("nightHours", detail.nightHours());
            dayDetail.put("performance", productivityPerDay.getOrDefault(date, BigDecimal.ZERO));
            dayDetail.put("isHoliday", isSundayOrHoliday(date));
            dailyDetails.add(dayDetail);
        }
        return objectMapper.writeValueAsString(dailyDetails);
    }

    private record DayHoursDetail(BigDecimal totalHours, BigDecimal nightHours) {}
    
    private record HoursBreakdown(
        BigDecimal normalHours,
        BigDecimal overtimeHours25,
        BigDecimal overtimeHours35,
        BigDecimal overtimeHours100,
        BigDecimal nightHours,
        BigDecimal totalHours
    ) {}
}
