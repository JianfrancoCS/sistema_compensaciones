package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.TareoEmployeeEntity;
import com.agropay.core.assignment.domain.TareoEntity;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.states.domain.StateEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TareoSpecification {

    public static Specification<TareoEntity> filterBy(
            UUID laborPublicId, 
            UUID subsidiaryPublicId, 
            String createdBy,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean isProcessed) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (laborPublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("labor").get("publicId"), laborPublicId));
            }

            if (subsidiaryPublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("subsidiary").get("publicId"), subsidiaryPublicId));
            }

            if (StringUtils.hasText(createdBy)) {
                // Filtrar por número de documento del supervisor
                Join<TareoEntity, com.agropay.core.organization.domain.EmployeeEntity> supervisorJoin = 
                    root.join("supervisor", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(supervisorJoin.get("personDocumentNumber")),
                    "%" + createdBy.toLowerCase() + "%"
                ));
            }

            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    dateFrom.atStartOfDay()
                ));
            }

            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    dateTo.atTime(23, 59, 59)
                ));
            }

            if (isProcessed != null) {
                // Un tareo está procesado si ya fue usado en una planilla calculada/aprobada/pagada
                // Verificar si existe PayrollDetailEntity para empleados de este tareo en planillas procesadas
                Subquery<Long> payrollSubquery = query.subquery(Long.class);
                var payrollDetailRoot = payrollSubquery.from(PayrollDetailEntity.class);
                Join<PayrollDetailEntity, PayrollEntity> payrollJoin = payrollDetailRoot.join("payroll", JoinType.INNER);
                Join<PayrollEntity, StateEntity> stateJoin = payrollJoin.join("state", JoinType.INNER);
                
                var tareoEmployeeSubquery = query.subquery(Long.class);
                var tareoEmployeeRoot = tareoEmployeeSubquery.from(TareoEmployeeEntity.class);
                
                tareoEmployeeSubquery.select(criteriaBuilder.literal(1L));
                tareoEmployeeSubquery.where(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(tareoEmployeeRoot.get("tareo").get("id"), root.get("id")),
                        criteriaBuilder.equal(tareoEmployeeRoot.get("employee").get("personDocumentNumber"), 
                                             payrollDetailRoot.get("employee").get("personDocumentNumber")),
                        criteriaBuilder.isNull(tareoEmployeeRoot.get("deletedAt")),
                        criteriaBuilder.between(
                            criteriaBuilder.function("CAST", java.time.LocalDate.class, 
                                criteriaBuilder.function("DATE", java.time.LocalDate.class, tareoEmployeeRoot.get("createdAt"))),
                            payrollJoin.get("periodStart"),
                            payrollJoin.get("periodEnd")
                        )
                    )
                );
                
                payrollSubquery.select(criteriaBuilder.literal(1L));
                payrollSubquery.where(
                    criteriaBuilder.and(
                        criteriaBuilder.exists(tareoEmployeeSubquery),
                        criteriaBuilder.in(stateJoin.get("code"))
                            .value("CALCULATED")
                            .value("APPROVED")
                            .value("PAID"),
                        criteriaBuilder.isNull(payrollDetailRoot.get("deletedAt"))
                    )
                );
                
                if (isProcessed) {
                    // Si isProcessed = true, debe estar en una planilla procesada
                    predicates.add(criteriaBuilder.exists(payrollSubquery));
                } else {
                    // Si isProcessed = false, no debe estar en ninguna planilla procesada
                    predicates.add(criteriaBuilder.not(criteriaBuilder.exists(payrollSubquery)));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}