package com.agropay.core.payroll.model.payslip;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Request para paginación y filtrado de boletas
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "payrollCode", "employeeDocumentNumber", "periodStart", "periodEnd", "netToPay", "createdAt"})
public class PayslipPageableRequest extends BasePageableRequest {

    // Filtros de fecha del período (rango de fechas que debe contener el período de la planilla)
    private LocalDate periodFrom;
    private LocalDate periodTo;
    
    // Filtro por empleado (se aplica automáticamente según el usuario autenticado)
    // Si el usuario es admin (sin employeeId), este campo puede ser usado para filtrar por cualquier empleado
    private String employeeDocumentNumber;
}

