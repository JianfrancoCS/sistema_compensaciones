package com.agropay.core.hiring.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractTypeEnum {
    INDEFINIDO("CONT_INDEFINIDO"),
    PLAZO("CONT_PLAZO");

    private final String code;
}