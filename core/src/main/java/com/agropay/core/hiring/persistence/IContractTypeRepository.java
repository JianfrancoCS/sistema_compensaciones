package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractTypeEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IContractTypeRepository extends ISoftRepository<ContractTypeEntity, Short>, JpaSpecificationExecutor<ContractTypeEntity> {
    Optional<ContractTypeEntity> findByPublicId(UUID publicId);
    Optional<ContractTypeEntity> findByCode(String code);
    boolean existsByName(String name);
}
