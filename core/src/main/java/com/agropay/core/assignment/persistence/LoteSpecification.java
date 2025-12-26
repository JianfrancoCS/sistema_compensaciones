package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.LoteEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoteSpecification {

    public static Specification<LoteEntity> filterBy(String name, UUID subsidiaryPublicId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }

            if (subsidiaryPublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("subsidiary").get("publicId"), subsidiaryPublicId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}