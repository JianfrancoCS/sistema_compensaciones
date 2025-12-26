package com.agropay.core.states.constant;

import com.agropay.core.shared.constant.StateEnum;

public enum AddendumStateEnum implements StateEnum {
    DRAFT("ADDENDUM_DRAFT", "Borrador"),
    SIGNED("ADDENDUM_SIGNED", "Firmado"),
    CANCELLED("ADDENDUM_CANCELLED", "Anulado"),
    EXPIRED("ADDENDUM_EXPIRED", "Vencido");

    private final String code;
    private final String displayName;

    AddendumStateEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}