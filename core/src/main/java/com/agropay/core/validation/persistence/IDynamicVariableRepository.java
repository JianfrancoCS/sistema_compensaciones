package com.agropay.core.validation.persistence;

import com.agropay.core.validation.domain.DynamicVariableEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDynamicVariableRepository extends ISoftRepository<DynamicVariableEntity, Short>, JpaSpecificationExecutor<DynamicVariableEntity> {
    Optional<DynamicVariableEntity> findByPublicId(UUID publicId);
    Optional<DynamicVariableEntity> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
    List<DynamicVariableEntity> findByIsActiveTrueOrderByName();
    Optional<DynamicVariableEntity> findByName(String name);
}