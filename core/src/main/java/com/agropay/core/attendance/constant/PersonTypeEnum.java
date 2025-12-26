package com.agropay.core.attendance.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PersonTypeEnum {
    EMPLOYEE("Empleado"),
    EXTERNAL("Externo");

    private final String description;
}