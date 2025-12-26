package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractSpecification {

    public static Specification<ContractEntity> filterBy(String contractNumber, String personDni,
                                                         UUID contractTypePublicId, UUID statePublicId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(contractNumber)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("contractNumber")),
                        "%" + contractNumber.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(personDni)) {
                predicates.add(criteriaBuilder.equal(root.get("personDocumentNumber"), personDni));
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
