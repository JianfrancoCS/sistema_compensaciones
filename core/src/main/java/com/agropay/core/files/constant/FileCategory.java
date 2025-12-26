package com.agropay.core.files.constant;

/**
 * Categorías de archivos internos del sistema
 */
public enum FileCategory {
    CONTRACT("CONTRACT", "Contrato laboral"),
    CONTRACT_INITIAL("CONTRACT_INITIAL", "Contrato inicial (sin firma empleador)"),
    CONTRACT_SIGNED("CONTRACT_SIGNED", "Contrato firmado (con ambas firmas)"),
    PAYSLIP("PAYSLIP", "Boleta de pago"),
    SIGNATURE("SIGNATURE", "Firma digital"),
    DOCUMENT("DOCUMENT", "Documento general"),
    REPORT("REPORT", "Reporte");

    private final String code;
    private final String description;

    FileCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

