package com.agropay.core.payroll.domain.enums;

/**
 * Enum para los códigos de categorías de conceptos de planilla.
 * Estos códigos deben coincidir con los valores en la tabla tbl_concept_categories.
 */
public enum ConceptCategoryCode {
    INCOME("INCOME", "Ingresos"),
    DEDUCTION("DEDUCTION", "Descuentos"),
    RETIREMENT("RETIREMENT", "Jubilación"),
    EMPLOYEE_CONTRIBUTION("EMPLOYEE_CONTRIBUTION", "Aportes del Empleado"),
    EMPLOYER_CONTRIBUTION("EMPLOYER_CONTRIBUTION", "Aportes del Empleador");

    private final String code;
    private final String displayName;

    ConceptCategoryCode(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ConceptCategoryCode fromCode(String code) {
        for (ConceptCategoryCode category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown concept category code: " + code);
    }
}

