package com.agropay.core.payroll.domain.enums;

import java.time.DayOfWeek;

/**
 * Enum para abreviaciones de días de la semana en español
 * Usado en boletas de pago peruanas
 */
public enum DayOfWeekAbbreviation {
    MONDAY(DayOfWeek.MONDAY, "LU"),
    TUESDAY(DayOfWeek.TUESDAY, "MA"),
    WEDNESDAY(DayOfWeek.WEDNESDAY, "MI"),
    THURSDAY(DayOfWeek.THURSDAY, "JU"),
    FRIDAY(DayOfWeek.FRIDAY, "VI"),
    SATURDAY(DayOfWeek.SATURDAY, "SA"),
    SUNDAY(DayOfWeek.SUNDAY, "DO");

    private final DayOfWeek dayOfWeek;
    private final String abbreviation;

    DayOfWeekAbbreviation(DayOfWeek dayOfWeek, String abbreviation) {
        this.dayOfWeek = dayOfWeek;
        this.abbreviation = abbreviation;
    }

    /**
     * Obtiene la abreviación para un DayOfWeek de Java
     * @param dayOfWeek Día de la semana de java.time.DayOfWeek
     * @return Abreviación en español (LU, MA, MI, etc.)
     */
    public static String getAbbreviation(DayOfWeek dayOfWeek) {
        for (DayOfWeekAbbreviation abbrev : values()) {
            if (abbrev.dayOfWeek == dayOfWeek) {
                return abbrev.abbreviation;
            }
        }
        return "";
    }

    /**
     * Obtiene la abreviación por número de día (1=Lunes, 7=Domingo)
     * @param dayValue Valor del día (1-7)
     * @return Abreviación en español
     */
    public static String getAbbreviation(int dayValue) {
        if (dayValue < 1 || dayValue > 7) {
            return "";
        }
        DayOfWeek dayOfWeek = DayOfWeek.of(dayValue);
        return getAbbreviation(dayOfWeek);
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}

