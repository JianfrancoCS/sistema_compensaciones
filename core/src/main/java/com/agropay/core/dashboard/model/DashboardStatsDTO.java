package com.agropay.core.dashboard.model;

public record DashboardStatsDTO(
        Long totalEmployees,
        Long totalPayrolls,
        Double totalPayrollsAmount,
        Long activeSubsidiaries,
        Long totalTareos,
        Long processedTareos,
        Long pendingPayrolls
) {
}

