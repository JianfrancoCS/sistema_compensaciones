package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.PersonEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PersonSpecification {

    public static Specification<PersonEntity> buildSpecification(String documentNumber, Boolean isNational) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por número de documento (siempre presente)
            if (documentNumber != null) {
                predicates.add(criteriaBuilder.equal(root.get("documentNumber"), documentNumber));
            }

            // Filtro por tipo de documento nacional/extranjero
            if (isNational != null) {
                if (isNational) {
                    // Solo personas con DNI
                    predicates.add(criteriaBuilder.equal(root.get("documentType").get("code"), "DNI"));
                } else {
                    // Solo personas con documentos extranjeros (diferente a DNI)
                    predicates.add(criteriaBuilder.notEqual(root.get("documentType").get("code"), "DNI"));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}