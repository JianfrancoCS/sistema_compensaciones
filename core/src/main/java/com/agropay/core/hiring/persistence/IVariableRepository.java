package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IVariableRepository extends ISoftRepository<VariableEntity, Short>, JpaSpecificationExecutor<VariableEntity> {
    Optional<VariableEntity> findByPublicId(UUID publicId);
    Optional<VariableEntity> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
