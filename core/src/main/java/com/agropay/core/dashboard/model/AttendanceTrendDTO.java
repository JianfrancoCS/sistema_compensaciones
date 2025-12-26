package com.agropay.core.dashboard.model;

import java.time.LocalDate;

public record AttendanceTrendDTO(
        LocalDate date,
        Long entries,
        Long exits
) {
}

