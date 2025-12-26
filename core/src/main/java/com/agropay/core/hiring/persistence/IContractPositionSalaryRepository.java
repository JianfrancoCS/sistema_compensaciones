package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractPositionSalaryEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IContractPositionSalaryRepository extends ISoftRepository<ContractPositionSalaryEntity, Long> {

    /**
     * Busca el salario especial activo para un contrato y posición específicos
     * Retorna el último registro activo (sin deleted_at) para ese contrato y posición
     */
    @Query("SELECT cps FROM ContractPositionSalaryEntity cps " +
           "WHERE cps.contract.id = :contractId " +
           "AND cps.position.id = :positionId " +
           "AND cps.deletedAt IS NULL " +
           "ORDER BY cps.createdAt DESC")
    Optional<ContractPositionSalaryEntity> findActiveByContractIdAndPositionId(
            @Param("contractId") Long contractId,
            @Param("positionId") Short positionId
    );
}

