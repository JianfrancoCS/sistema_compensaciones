package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.DocumentTypeEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDocumentTypeRepository extends ISoftRepository<DocumentTypeEntity, Short> {

    Optional<DocumentTypeEntity> findByCode(String code);

    Optional<DocumentTypeEntity> findByPublicId(UUID publicId);
}