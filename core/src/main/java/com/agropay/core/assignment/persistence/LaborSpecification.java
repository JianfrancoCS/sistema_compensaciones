package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.LaborEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LaborSpecification {

    public static Specification<LaborEntity> filterBy(String name, Boolean isPiecework, UUID laborUnitPublicId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }

            if (isPiecework != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPiecework"), isPiecework));
            }

            if (laborUnitPublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("laborUnit").get("publicId"), laborUnitPublicId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}