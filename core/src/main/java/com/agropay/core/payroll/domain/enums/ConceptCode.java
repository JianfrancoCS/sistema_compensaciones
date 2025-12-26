package com.agropay.core.payroll.domain.enums;

/**
 * Enum de códigos de conceptos de planilla
 * IMPORTANTE: Los valores deben coincidir EXACTAMENTE con los códigos en la migración V094__Insert_payroll_concepts.sql
 * Este es el ÚNICO punto de verdad para los códigos de conceptos
 */
public enum ConceptCode {
    BASIC_SALARY("BASICO"),
    AFP_INTEGRA("FONDO AFP"), // Se muestra como FONDO AFP en boletas
    AFP_PRIMA("FONDO AFP"),
    AFP_PROFUTURO("FONDO AFP"),
    AFP_HABITAT("FONDO AFP"),
    ONP("ONP"),
    ESSALUD("ESSALUD"),
    SEGURO_VIDA_LEY("VIDA LEY"),
    OVERTIME("IMP HOR EXT 25%"),
    ATTENDANCE_BONUS("BON. X ASIS. SD"),
    PRODUCTIVITY_BONUS("B PRODUCTI ARAN"),
    PIECEWORK_EXCESS("DESTAJO EXCEDENTE"),
    DOMINICAL("DOMINICAL"),
    FAMILY_ALLOWANCE("ASIG. FAMILIAR");

    /**
     * Nombre de visualización para boletas de pago (formato peruano)
     * Este es el texto que aparece en las boletas de pago
     */
    private final String payslipDisplayName;

    ConceptCode(String payslipDisplayName) {
        this.payslipDisplayName = payslipDisplayName;
    }

    /**
     * Obtiene el nombre de visualización para boletas de pago
     * @return Nombre formateado según estándar peruano de boletas
     */
    public String getPayslipDisplayName() {
        return payslipDisplayName;
    }

    /**
     * Obtiene el nombre de visualización para un código de concepto
     * Si el código no existe en el enum, retorna el código original
     * 
     * @param code Código del concepto (debe coincidir con el enum)
     * @return Nombre de visualización para boleta
     */
    public static String getPayslipDisplayName(String code) {
        try {
            ConceptCode conceptCode = ConceptCode.valueOf(code);
            return conceptCode.getPayslipDisplayName();
        } catch (IllegalArgumentException e) {
            // Si el código no existe en el enum, retornar el código original
            // Esto permite manejar conceptos dinámicos o futuros sin romper el sistema
            return code;
        }
    }

    public static ConceptCode fromCode(String code) {
        try {
            return ConceptCode.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown concept code: " + code);
        }
    }
}