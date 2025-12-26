package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.TareoEntity;
import com.agropay.core.assignment.domain.TareoEmployeeEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITareoRepository extends ISoftRepository<TareoEntity, Integer>,
        IFindByPublicIdRepository<TareoEntity>,
        JpaSpecificationExecutor<TareoEntity> {

    @Query("SELECT COUNT(te) FROM TareoEmployeeEntity te WHERE te.tareo.id = :tareoId AND te.deletedAt IS NULL")
    long countTareoEmployeesByTareoId(@Param("tareoId") Integer tareoId);

    Optional<TareoEntity> findByTemporalIdAndDeletedAtIsNull(String temporalId);

    // Dashboard queries
    @Query("""
        SELECT l.name, COUNT(DISTINCT t), COUNT(DISTINCT te)
        FROM TareoEntity t
        JOIN t.labor l
        LEFT JOIN TareoEmployeeEntity te ON te.tareo.id = t.id AND te.deletedAt IS NULL
        WHERE t.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR t.subsidiary.id = :subsidiaryId)
        AND (:dateFrom IS NULL OR CAST(t.createdAt AS date) >= :dateFrom)
        AND (:dateTo IS NULL OR CAST(t.createdAt AS date) <= :dateTo)
        GROUP BY l.id, l.name
        ORDER BY COUNT(DISTINCT t) DESC
    """)
    List<Object[]> getTareosByLaborGrouped(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo
    );

    @Query("""
        SELECT COUNT(t)
        FROM TareoEntity t
        WHERE t.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR t.subsidiary.id = :subsidiaryId)
        AND (:dateFrom IS NULL OR CAST(t.createdAt AS date) >= :dateFrom)
        AND (:dateTo IS NULL OR CAST(t.createdAt AS date) <= :dateTo)
    """)
    Long countByFilters(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo
    );

    @Query("""
        SELECT COUNT(DISTINCT t)
        FROM TareoEntity t
        INNER JOIN TareoEmployeeEntity te ON te.tareo.id = t.id AND te.deletedAt IS NULL
        WHERE t.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR t.subsidiary.id = :subsidiaryId)
        AND (:dateFrom IS NULL OR CAST(t.createdAt AS date) >= :dateFrom)
        AND (:dateTo IS NULL OR CAST(t.createdAt AS date) <= :dateTo)
    """)
    Long countProcessedByFilters(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo
    );

    /**
     * Cuenta los empleados únicos que tienen tareos en el período y subsidiaria especificados.
     * Usa la misma lógica que EmployeeReader para asegurar consistencia.
     */
    @Query("""
        SELECT COUNT(DISTINCT e.id)
        FROM com.agropay.core.organization.domain.EmployeeEntity e
        WHERE EXISTS (
            SELECT 1 FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te
            JOIN te.tareo t
            WHERE te.employee.personDocumentNumber = e.personDocumentNumber
            AND CAST(t.createdAt AS date) BETWEEN :periodStart AND :periodEnd
            AND t.subsidiary.id = :subsidiaryId
            AND te.deletedAt IS NULL
            AND t.deletedAt IS NULL
        )
        AND e.deletedAt IS NULL
    """)
    Long countEmployeesWithTareosInPeriod(
            @Param("periodStart") java.time.LocalDate periodStart,
            @Param("periodEnd") java.time.LocalDate periodEnd,
            @Param("subsidiaryId") Short subsidiaryId
    );

    /**
     * Cuenta los tareos únicos en el período y subsidiaria especificados.
     */
    @Query("""
        SELECT COUNT(DISTINCT t.id)
        FROM TareoEntity t
        WHERE CAST(t.createdAt AS date) BETWEEN :periodStart AND :periodEnd
        AND t.subsidiary.id = :subsidiaryId
        AND t.deletedAt IS NULL
    """)
    Long countTareosInPeriod(
            @Param("periodStart") java.time.LocalDate periodStart,
            @Param("periodEnd") java.time.LocalDate periodEnd,
            @Param("subsidiaryId") Short subsidiaryId
    );
}
