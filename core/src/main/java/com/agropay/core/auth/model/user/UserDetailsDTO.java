package com.agropay.core.auth.model.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UserDetailsDTO(
        // Información del usuario
        UUID userPublicId,
        String username,
        Boolean isActive,
        
        // Información del empleado (puede ser null)
        EmployeeInfo employee,
        
        // Información del contrato (puede ser null)
        ContractInfo contract
) {
    public record EmployeeInfo(
            UUID employeeCode,
            String documentNumber,
            String names,
            String paternalLastname,
            String maternalLastname,
            LocalDate dateOfBirth,
            String gender,
            
            // Información del cargo
            UUID positionId,
            String positionName,
            BigDecimal positionSalary, // Salario fijo del cargo
            
            // Información del empleado
            BigDecimal customSalary, // Salario personalizado (si existe)
            BigDecimal dailyBasicSalary,
            LocalDate hireDate,
            String subsidiaryName,
            String stateName,
            String afpAffiliationNumber,
            String bankAccountNumber,
            String bankName
    ) {}
    
    public record ContractInfo(
            UUID contractPublicId,
            String contractNumber,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate extendedEndDate,
            String contractTypeName,
            String stateName
    ) {}
}

