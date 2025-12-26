package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.AreaEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IAreaRepository extends ISoftRepository<AreaEntity, Short>,
        IFindByPublicIdRepository<AreaEntity>,
        JpaSpecificationExecutor<AreaEntity> {

    Optional<AreaEntity> findByNameIgnoreCase(String name);

    Page<AreaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"positions"})
    Optional<AreaEntity> findWithPositionsByPublicId(UUID publicId);
}
