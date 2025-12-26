package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.CompanySubsidiarySignerEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para responsables de firma de boletas de pago
 * Tabla histórica: el último registro (más reciente) es el que se usa
 */
@Repository
public interface ICompanySubsidiarySignerRepository extends 
        ISoftRepository<CompanySubsidiarySignerEntity, Long>,
        IFindByPublicIdRepository<CompanySubsidiarySignerEntity> {

    /**
     * Busca el responsable más reciente para una empresa y subsidiaria específica
     * Si no encuentra para la subsidiaria, busca a nivel de empresa (subsidiary_id = NULL)
     * 
     * @param companyId ID de la empresa
     * @param subsidiaryId ID de la subsidiaria (puede ser null)
     * @return El responsable más reciente o vacío si no existe
     */
    @Query(value = """
        SELECT TOP 1 * FROM app.tbl_company_subsidiary_signers
        WHERE company_id = :companyId
        AND (subsidiary_id = :subsidiaryId OR (subsidiary_id IS NULL AND :subsidiaryId IS NULL))
        AND deleted_at IS NULL
        ORDER BY created_at DESC
    """, nativeQuery = true)
    Optional<CompanySubsidiarySignerEntity> findLatestByCompanyAndSubsidiary(
        @Param("companyId") Long companyId,
        @Param("subsidiaryId") Short subsidiaryId
    );

    /**
     * Busca el responsable más reciente a nivel de empresa (subsidiary_id = NULL)
     * 
     * @param companyId ID de la empresa
     * @return El responsable más reciente a nivel de empresa o vacío si no existe
     */
    @Query(value = """
        SELECT TOP 1 * FROM app.tbl_company_subsidiary_signers
        WHERE company_id = :companyId
        AND subsidiary_id IS NULL
        AND deleted_at IS NULL
        ORDER BY created_at DESC
    """, nativeQuery = true)
    Optional<CompanySubsidiarySignerEntity> findLatestByCompany(
        @Param("companyId") Long companyId
    );

    /**
     * Busca todos los responsables de firma para una subsidiaria específica (historial)
     * 
     * @param companyId ID de la empresa
     * @param subsidiaryId ID de la subsidiaria
     * @return Lista de responsables ordenados por fecha de creación descendente
     */
    @Query(value = """
        SELECT * FROM app.tbl_company_subsidiary_signers
        WHERE company_id = :companyId
        AND subsidiary_id = :subsidiaryId
        AND deleted_at IS NULL
        ORDER BY created_at DESC
    """, nativeQuery = true)
    List<CompanySubsidiarySignerEntity> findAllByCompanyAndSubsidiary(
        @Param("companyId") Long companyId,
        @Param("subsidiaryId") Short subsidiaryId
    );

}

