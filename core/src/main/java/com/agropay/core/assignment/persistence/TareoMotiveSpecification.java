package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.TareoMotiveEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TareoMotiveSpecification {

    public static Specification<TareoMotiveEntity> filterBy(String name, Boolean isPaid) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }

            if (isPaid != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPaid"), isPaid));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}