package com.agropay.core.payroll.persistence.specification;

import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.model.payslip.PayslipPageableRequest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PayslipSpecifications {

    public static Specification<PayrollDetailEntity> from(PayslipPageableRequest request, String employeeDocumentNumber) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtrar por empleado del usuario autenticado (si no es admin)
            if (employeeDocumentNumber != null && !employeeDocumentNumber.isBlank()) {
                predicates.add(cb.equal(root.get("employee").get("personDocumentNumber"), employeeDocumentNumber));
            }

            // Si el request tiene un employeeDocumentNumber específico (para admins), usarlo
            if (request.getEmployeeDocumentNumber() != null && !request.getEmployeeDocumentNumber().isBlank()) {
                predicates.add(cb.equal(root.get("employee").get("personDocumentNumber"), request.getEmployeeDocumentNumber()));
            }

            // Filtros de fecha del período
            // Si se proporciona periodFrom, el período debe terminar después o en esa fecha
            if (request.getPeriodFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("payroll").get("periodEnd"), request.getPeriodFrom()));
            }

            // Si se proporciona periodTo, el período debe empezar antes o en esa fecha
            if (request.getPeriodTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("payroll").get("periodStart"), request.getPeriodTo()));
            }

            // Solo mostrar boletas que tienen PDF generado (payslip_pdf_url IS NOT NULL)
            predicates.add(cb.isNotNull(root.get("payslipPdfUrl")));
            predicates.add(cb.notEqual(root.get("payslipPdfUrl"), ""));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

