package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IContractTemplateRepository extends ISoftRepository<ContractTemplateEntity, Short>, JpaSpecificationExecutor<ContractTemplateEntity> {
    Optional<ContractTemplateEntity> findByPublicId(UUID publicId);
    long countByContractTypeId(Short contractTypeId);
    boolean existsByName(String name);
    Optional<ContractTemplateEntity> findByName(String name);
    List<ContractTemplateEntity> findAllByContractType_PublicId(UUID contractTypePublicId);
}
