package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.LaborUnitEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LaborUnitSpecification {

    public static Specification<LaborUnitEntity> filterBy(String name, String abbreviation) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(abbreviation)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("abbreviation")),
                    "%" + abbreviation.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}