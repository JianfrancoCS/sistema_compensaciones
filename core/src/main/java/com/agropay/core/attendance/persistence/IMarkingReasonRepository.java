package com.agropay.core.attendance.persistence;

import com.agropay.core.attendance.domain.MarkingReasonEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMarkingReasonRepository extends ISoftRepository<MarkingReasonEntity, Short>, JpaSpecificationExecutor<MarkingReasonEntity> {

    Optional<MarkingReasonEntity> findByPublicId(UUID publicId);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByNameAndDeletedAtIsNull(String name);

    Optional<MarkingReasonEntity> findByCodeAndDeletedAtIsNull(String code);

    List<MarkingReasonEntity> findByIsInternalAndDeletedAtIsNull(Boolean isInternal, Sort sort);
}