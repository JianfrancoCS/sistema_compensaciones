package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractTemplateEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractTemplateSpecification {

    public static Specification<ContractTemplateEntity> filterBy(String code, String name, UUID contractTypePublicId, UUID statePublicId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(code)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")),
                        "%" + code.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }

            if (contractTypePublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("contractType").get("publicId"), contractTypePublicId));
            }

            if (statePublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("state").get("publicId"), statePublicId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
