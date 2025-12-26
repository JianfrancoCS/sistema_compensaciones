package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.AddendumTemplateEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAddendumTemplateRepository extends ISoftRepository<AddendumTemplateEntity, Short>, JpaSpecificationExecutor<AddendumTemplateEntity> {
    Optional<AddendumTemplateEntity> findByPublicId(UUID publicId);
    Optional<AddendumTemplateEntity> findByCode(String code);
    Optional<AddendumTemplateEntity> findByName(String name);
    List<AddendumTemplateEntity> findAllByAddendumTypePublicId(UUID addendumTypePublicId);
    long countByAddendumTypeId(Short addendumTypeId);
    boolean existsByName(String name);
}