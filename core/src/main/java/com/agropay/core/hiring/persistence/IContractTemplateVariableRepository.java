package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractTemplateEntity;
import com.agropay.core.hiring.domain.ContractTemplateVariableEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IContractTemplateVariableRepository extends ISoftRepository<ContractTemplateVariableEntity, Short> {
    List<ContractTemplateVariableEntity> findByContractTemplateOrderByDisplayOrder(ContractTemplateEntity contractTemplate);
    void deleteByContractTemplate(ContractTemplateEntity contractTemplate);
}
