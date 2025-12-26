package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPositionRepository extends ISoftRepository<PositionEntity, Short>,
        IFindByPublicIdRepository<PositionEntity>,
        JpaSpecificationExecutor<PositionEntity> {

    Optional<PositionEntity> findByNameIgnoreCase(String name);

    Page<PositionEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByAreaId(Short areaId);

    long countByAreaId(Short areaId);

    Optional<PositionEntity> findByNameAndAreaId(String name, Short areaId);
}
