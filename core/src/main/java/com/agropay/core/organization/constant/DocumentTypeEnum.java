package com.agropay.core.organization.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentTypeEnum {
    DNI("DNI", "Documento Nacional de Identidad"),
    CARNET_EXTRANJERIA("CE", "Carnet de Extranjería");

    private final String code;
    private final String displayName;
}