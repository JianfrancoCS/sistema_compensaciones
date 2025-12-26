package com.agropay.core.organization.model.employee;

import java.util.List;
import java.util.UUID;

public record OrganizationalChartNodeDTO(
        Boolean expanded,
        EmployeeDataDTO data,
        List<OrganizationalChartNodeDTO> children
) {
    public record EmployeeDataDTO(
            UUID employeeCode,
            String image,
            String name,
            String title
    ) {}
}
