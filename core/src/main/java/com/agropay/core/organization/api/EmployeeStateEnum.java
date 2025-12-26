package com.agropay.core.organization.api;

import com.agropay.core.shared.constant.StateEnum;

public enum EmployeeStateEnum implements StateEnum {
    ACTIVO("EMPLOYEE_ACTIVE", "Activo"),
    CREADO("EMPLOYEE_CREATE", "Creado"),
    INACTIVO("EMPLOYEE_INACTIVE", "Inactivo");

    private final String code;
    private final String displayName;

    EmployeeStateEnum(String code, String displayName) {
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