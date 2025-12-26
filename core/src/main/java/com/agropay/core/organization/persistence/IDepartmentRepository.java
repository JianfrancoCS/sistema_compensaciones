package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.DepartmentEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDepartmentRepository extends ISoftRepository<DepartmentEntity, Integer> {
}
