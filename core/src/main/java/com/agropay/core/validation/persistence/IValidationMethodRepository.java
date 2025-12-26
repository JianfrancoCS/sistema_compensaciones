package com.agropay.core.validation.persistence;

import com.agropay.core.validation.domain.ValidationMethodEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IValidationMethodRepository extends ISoftRepository<ValidationMethodEntity, Short>, JpaSpecificationExecutor<ValidationMethodEntity> {
    Optional<ValidationMethodEntity> findByPublicId(UUID publicId);
    Optional<ValidationMethodEntity> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
    List<ValidationMethodEntity> findByPublicIdIn(List<UUID> publicIds);
}