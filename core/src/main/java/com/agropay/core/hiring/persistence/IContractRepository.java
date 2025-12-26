package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.ContractEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IContractRepository extends ISoftRepository<ContractEntity, Long>, JpaSpecificationExecutor<ContractEntity> {
    Optional<ContractEntity> findByPublicId(UUID publicId);
    long countByContractTypeId(Short contractTypeId);
    Optional<ContractEntity> findByContractNumber(String contractNumber);
    
    /**
     * Busca contratos por número de documento de persona.
     * Puede retornar múltiples contratos si hay varios activos.
     * 
     * @deprecated Usar findByPersonDocumentNumberAndSubsidiaryPublicId para búsquedas más específicas
     */
    @Query("SELECT c FROM ContractEntity c WHERE c.personDocumentNumber = :personDocumentNumber AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<ContractEntity> findAllByPersonDocumentNumber(@Param("personDocumentNumber") String personDocumentNumber);
    
    /**
     * Busca el contrato activo (FIRMADO/SIGNED) de una persona para una subsidiaria específica.
     * Solo puede haber un contrato activo por persona, los demás deben estar anulados/cancelados.
     * Útil para validaciones de marcado de asistencia.
     * 
     * @param personDocumentNumber Número de documento de la persona
     * @param subsidiaryPublicId ID público de la subsidiaria
     * @param signedStateCode Código del estado SIGNED (firmado)
     * @return El contrato activo (SIGNED) que coincida con los criterios, o vacío si no existe
     */
    @Query("""
        SELECT c FROM ContractEntity c
        JOIN c.state s
        WHERE c.personDocumentNumber = :personDocumentNumber
        AND c.subsidiary.publicId = :subsidiaryPublicId
        AND s.code = :signedStateCode
        AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC
        """)
    Optional<ContractEntity> findActiveContractByPersonDocumentNumberAndSubsidiary(
        @Param("personDocumentNumber") String personDocumentNumber,
        @Param("subsidiaryPublicId") UUID subsidiaryPublicId,
        @Param("signedStateCode") String signedStateCode
    );
    
    /**
     * Busca todos los contratos activos (FIRMADO/SIGNED) de una persona.
     * 
     * @param personDocumentNumber Número de documento de la persona
     * @param signedStateCode Código del estado SIGNED (firmado)
     * @return Lista de contratos activos (SIGNED) de la persona
     */
    @Query("""
        SELECT c FROM ContractEntity c
        JOIN c.state s
        WHERE c.personDocumentNumber = :personDocumentNumber
        AND s.code = :signedStateCode
        AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC
        """)
    List<ContractEntity> findAllActiveContractsByPersonDocumentNumber(
        @Param("personDocumentNumber") String personDocumentNumber,
        @Param("signedStateCode") String signedStateCode
    );
    
    /**
     * Método legacy que busca un único contrato por persona.
     * Si hay múltiples contratos, retorna el más reciente.
     * 
     * @deprecated Usar findAllByPersonDocumentNumber o findMostRecentByPersonDocumentNumberAndSubsidiaryPublicId
     */
    @Query("""
        SELECT c FROM ContractEntity c
        WHERE c.personDocumentNumber = :personDocumentNumber
        AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC
        """)
    Optional<ContractEntity> findByPersonDocumentNumber(@Param("personDocumentNumber") String personDocumentNumber);
}
