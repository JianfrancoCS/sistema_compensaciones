package com.agropay.core.states.persistence;

import com.agropay.core.states.domain.DomainEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DomainRepository extends ISoftRepository<DomainEntity, Short> {
    Optional<DomainEntity> findByName(String name);
}
