package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.ConceptCategoryEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IConceptCategoryRepository extends ISoftRepository<ConceptCategoryEntity, Short> {

    Optional<ConceptCategoryEntity> findByCode(String code);

    Optional<ConceptCategoryEntity> findByPublicId(UUID publicId);
}