package com.agropay.core.attendance.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarkingReasonEnum {
    WORK("WORK", "Laborar", PersonTypeEnum.EMPLOYEE),
    CONTRACT_SIGNING("CONTRACT_SIGNING", "Firmar Contrato", PersonTypeEnum.EXTERNAL),
    VISIT("VISIT", "Visita General", PersonTypeEnum.EXTERNAL),
    SUPERVISION("SUPERVISION", "Supervisión/Inspección", PersonTypeEnum.EXTERNAL);

    private final String code;
    private final String description;
    private final PersonTypeEnum allowedPersonType;

    public static MarkingReasonEnum findByCode(String code) {
        for (MarkingReasonEnum reason : values()) {
            if (reason.getCode().equals(code)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("No se encontró MarkingReason con código: " + code);
    }

    public boolean isValidForPersonType(PersonTypeEnum personType) {
        return this.allowedPersonType == personType;
    }
}