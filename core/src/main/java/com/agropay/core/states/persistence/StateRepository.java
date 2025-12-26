package com.agropay.core.states.persistence;

import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends ISoftRepository<StateEntity, Short> {

    List<StateEntity> findByDomainName(@Param("domainName") String domainName);
    Optional<StateEntity> findByPublicId(UUID publicId);
    Optional<StateEntity> findByIsDefaultTrueAndDomainName(String domainName);
    Optional<StateEntity> findByCodeAndDomainName(String code, String domain);
}
