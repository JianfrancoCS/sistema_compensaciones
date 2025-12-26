package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.QrCodeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QrCodeSpecification {

    public static Specification<QrCodeEntity> filterByRoll(Integer rollId, Boolean isUsed, Boolean isPrinted, LocalDate createdDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro obligatorio por rollId
            predicates.add(criteriaBuilder.equal(root.get("qrRoll").get("id"), rollId));

            // Filtro opcional por isUsed
            if (isUsed != null) {
                predicates.add(criteriaBuilder.equal(root.get("isUsed"), isUsed));
            }

            // Filtro opcional por isPrinted
            if (isPrinted != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPrinted"), isPrinted));
            }

            // Filtro opcional por fecha de creación
            if (createdDate != null) {
                LocalDateTime startOfDay = createdDate.atStartOfDay();
                LocalDateTime endOfDay = createdDate.plusDays(1).atStartOfDay();
                predicates.add(criteriaBuilder.between(root.get("createdAt"), startOfDay, endOfDay));
            }

            // Ordenar por createdAt DESC
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}