package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IConceptRepository extends ISoftRepository<ConceptEntity, Short>, JpaSpecificationExecutor<ConceptEntity> {
    // JpaRepository's findAll() will be automatically filtered by @SQLRestriction("deleted_at IS NULL")

    List<ConceptEntity> findByPublicIdIn(List<UUID> publicIds);

    Optional<ConceptEntity> findByPublicId(UUID publicId);
}
