package com.agropay.core.payroll.persistence.specification;

import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.model.payroll.PayrollPageableRequest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PayrollSpecifications {

    public static Specification<PayrollEntity> from(PayrollPageableRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getSubsidiaryPublicId() != null) {
                predicates.add(cb.equal(root.get("subsidiary").get("publicId"), request.getSubsidiaryPublicId()));
            }

            if (request.getPeriodPublicId() != null) {
                predicates.add(cb.equal(root.get("period").get("publicId"), request.getPeriodPublicId()));
            }

            if (request.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), request.getYear()));
            }

            if (request.getMonth() != null) {
                predicates.add(cb.equal(root.get("month"), request.getMonth()));
            }

            if (request.getStatePublicId() != null) {
                predicates.add(cb.equal(root.get("state").get("publicId"), request.getStatePublicId()));
            }

            // Si se proporciona status (código), filtrar por código de estado
            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("state").get("code"), request.getStatus()));
            }

            if (request.getPeriodStartFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("periodStart"), request.getPeriodStartFrom()));
            }

            if (request.getPeriodStartTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("periodStart"), request.getPeriodStartTo()));
            }

            if (request.getPeriodEndFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("periodEnd"), request.getPeriodEndFrom()));
            }

            if (request.getPeriodEndTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("periodEnd"), request.getPeriodEndTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
