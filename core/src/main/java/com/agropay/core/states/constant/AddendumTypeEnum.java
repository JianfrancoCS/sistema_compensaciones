package com.agropay.core.states.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AddendumTypeEnum {
    DURATION("ADDEN_PLAZO"),
    ECONOMIC("ADDEN_ECONOMICO"),
    SCOPE("ADDEN_ALCANCE"),
    CONDITIONS("ADDEN_CONDICIONES");

    private final String code;
}
