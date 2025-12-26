package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.TareoEmployeeMotiveEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITareoEmployeeMotiveRepository extends ISoftRepository<TareoEmployeeMotiveEntity, Long> {
}