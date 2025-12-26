package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.AddendumEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractAddendumSpecification {

    public static Specification<AddendumEntity> filterBy(String addendumNumber, UUID contractPublicId, UUID addendumTypePublicId, UUID statePublicId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(addendumNumber)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("addendumNumber")),
                        "%" + addendumNumber.toLowerCase() + "%"));
            }

            if (contractPublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("contract").get("publicId"), contractPublicId));
            }

            if (addendumTypePublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("addendumType").get("publicId"), addendumTypePublicId));
            }

            if (statePublicId != null) {
                predicates.add(criteriaBuilder.equal(root.get("state").get("publicId"), statePublicId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}