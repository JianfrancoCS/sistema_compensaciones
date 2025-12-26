package com.agropay.core.payroll.model.payroll;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request para paginación y filtrado de planillas
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "code", "year", "month", "periodStart", "periodEnd", "createdAt", "updatedAt", "approvedAt"})
public class PayrollPageableRequest extends BasePageableRequest {

    // Filtros
    private UUID subsidiaryPublicId;
    private UUID periodPublicId; // Para filtrar por período
    private Short year;
    private Short month;
    private UUID statePublicId;
    private String status; // Código de estado (ej: "PAYROLL_DRAFT", "PAYROLL_CALCULATED") - se mapea a statePublicId

    // Rango de fechas del período
    private LocalDate periodStartFrom;
    private LocalDate periodStartTo;
    private LocalDate periodEndFrom;
    private LocalDate periodEndTo;
}