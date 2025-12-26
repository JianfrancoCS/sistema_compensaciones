package com.agropay.core.payroll.model.payroll;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para el detalle completo de un empleado en una planilla
 */
public record PayrollEmployeeDetailDTO(
    UUID publicId,
    String employeeDocumentNumber,
    String employeeFullName,
    String positionName,
    BigDecimal totalIncome,
    BigDecimal totalDeductions,
    BigDecimal totalEmployerContributions,
    BigDecimal netToPay,
    Short daysWorked,
    BigDecimal normalHours,
    BigDecimal overtimeHours25,
    BigDecimal overtimeHours35,
    BigDecimal overtimeHours100,
    BigDecimal nightHours,
    Map<String, Object> calculatedConcepts,
    List<DailyWorkDetail> dailyDetails
) {
    public record DailyWorkDetail(
        String date,
        String dayOfWeek,
        BigDecimal hours,
        BigDecimal nightHours,
        BigDecimal performancePercentage, // Porcentaje de productividad
        Long productivityValue, // Valor numérico de productividad (harvestCount)
        String productivityUnit, // Unidad de medida (ej: "Jarras", "Jabas")
        Boolean isHoliday, // Indica si es feriado (tiene evento HOLIDAY)
        Boolean isNonWorkingDay, // Indica si es día no laborable (domingo o feriado)
        Boolean worked // Indica si el empleado trabajó ese día
    ) {}
}

