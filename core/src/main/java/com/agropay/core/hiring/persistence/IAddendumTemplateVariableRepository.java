package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.AddendumTemplateVariableEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAddendumTemplateVariableRepository extends ISoftRepository<AddendumTemplateVariableEntity, Short> {
}