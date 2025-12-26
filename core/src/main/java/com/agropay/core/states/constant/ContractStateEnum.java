package com.agropay.core.states.constant;

import com.agropay.core.shared.constant.StateEnum;

public enum ContractStateEnum implements StateEnum {
    DRAFT("CONTRACT_DRAFT", "Borrador"),
    SIGNED("CONTRACT_SIGNED", "Firmado"),
    CANCELLED("CONTRACT_CANCELLED", "Anulado"),
    EXPIRED("CONTRACT_EXPIRED", "Vencido");

    private final String code;
    private final String displayName;

    ContractStateEnum(String code, String displayName) {
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