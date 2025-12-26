package com.agropay.core.payroll.domain.calculator;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;

/**
 * Información de un día del calendario para cálculos de planilla
 */
@Data
@Builder
public class WorkCalendarDayInfo {
    private boolean isWorkingDay; // Si es día laboral según calendario de la empresa
    private boolean isHoliday; // Si es feriado
    private boolean isSunday; // Si es domingo
    private DayOfWeek dayOfWeek; // Día de la semana
}

