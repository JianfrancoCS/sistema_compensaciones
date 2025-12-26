package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.AddendumTypeEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAddendumTypeRepository extends ISoftRepository<AddendumTypeEntity, Short>, JpaSpecificationExecutor<AddendumTypeEntity> {
    Optional<AddendumTypeEntity> findByPublicId(UUID publicId);
    Optional<AddendumTypeEntity> findByCode(String code);
    boolean existsByName(String name);
}