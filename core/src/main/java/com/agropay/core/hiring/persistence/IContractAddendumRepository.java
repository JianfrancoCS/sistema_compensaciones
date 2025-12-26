package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.AddendumEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IContractAddendumRepository extends ISoftRepository<AddendumEntity, Long>, JpaSpecificationExecutor<AddendumEntity> {
    Optional<AddendumEntity> findByPublicId(UUID publicId);
    boolean existsByAddendumNumber(String addendumNumber);
    long countByAddendumTypeId(Short addendumTypeId);
    boolean existsByTemplate_Id(Short templateId);
    List<AddendumEntity> findByContract_PublicIdOrderByCreatedAtDesc(UUID contractPublicId);
    boolean existsByContract_PublicIdAndAddendumType_PublicId(UUID contractPublicId, UUID addendumTypePublicId);
}