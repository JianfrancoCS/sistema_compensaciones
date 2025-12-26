package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractVariableValueEntity;
import com.agropay.core.hiring.domain.ContractVariableValueId;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IContractVariableValueRepository extends ISoftRepository<ContractVariableValueEntity, ContractVariableValueId> {
    
    @Query("SELECT cvv FROM ContractVariableValueEntity cvv " +
           "WHERE cvv.contract.id = :contractId AND cvv.deletedAt IS NULL")
    List<ContractVariableValueEntity> findByContractId(@Param("contractId") Long contractId);
}
