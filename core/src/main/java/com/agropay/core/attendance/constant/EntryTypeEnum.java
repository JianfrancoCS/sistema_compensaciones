package com.agropay.core.attendance.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntryTypeEnum {
    ENTRY(true, "Entrada"),
    EXIT(false, "Salida");

    private final boolean isEntry;
    private final String description;

    public static EntryTypeEnum fromBoolean(boolean isEntry) {
        return isEntry ? ENTRY : EXIT;
    }
}