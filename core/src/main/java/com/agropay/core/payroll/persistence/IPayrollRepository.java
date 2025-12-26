package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPayrollRepository extends ISoftRepository<PayrollEntity, Long>, JpaSpecificationExecutor<PayrollEntity> {

    Optional<PayrollEntity> findByCode(String code);

    Optional<PayrollEntity> findByPublicId(UUID publicId);

    @Query("SELECT p FROM PayrollEntity p WHERE p.subsidiary.id = :subsidiaryId AND p.year = :year AND p.month = :month")
    Optional<PayrollEntity> findBySubsidiaryAndPeriod(
        @Param("subsidiaryId") Short subsidiaryId,
        @Param("year") Short year,
        @Param("month") Short month
    );

    @Query("SELECT p FROM PayrollEntity p WHERE p.subsidiary.id = :subsidiaryId ORDER BY p.year DESC, p.month DESC")
    List<PayrollEntity> findBySubsidiaryOrderByPeriodDesc(@Param("subsidiaryId") Short subsidiaryId);

    @Query("SELECT p FROM PayrollEntity p WHERE p.subsidiary.id = :subsidiaryId AND p.state.code = :stateCode")
    List<PayrollEntity> findBySubsidiaryAndStateCode(
        @Param("subsidiaryId") Short subsidiaryId,
        @Param("stateCode") String stateCode
    );

    boolean existsByPeriodId(Integer periodId);

    boolean existsByPayrollConfigurationId(Long payrollConfigurationId);

    // Dashboard queries
    @Query("""
        SELECT s.code, COUNT(p), COALESCE(SUM(p.totalNet), 0)
        FROM PayrollEntity p
        JOIN p.state s
        WHERE p.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR p.subsidiary.id = :subsidiaryId)
        AND (:periodId IS NULL OR p.period.id = :periodId)
        GROUP BY s.code
    """)
    List<Object[]> getPayrollsByStatusGrouped(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("periodId") Integer periodId
    );

    @Query("""
        SELECT p.year, p.month, COUNT(p), COALESCE(SUM(p.totalNet), 0)
        FROM PayrollEntity p
        WHERE p.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR p.subsidiary.id = :subsidiaryId)
        AND (:dateFrom IS NULL OR p.periodStart >= :dateFrom)
        AND (:dateTo IS NULL OR p.periodStart <= :dateTo)
        GROUP BY p.year, p.month
        ORDER BY p.year DESC, p.month DESC
    """)
    List<Object[]> getPayrollsByPeriodGrouped(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("dateFrom") java.time.LocalDate dateFrom,
            @Param("dateTo") java.time.LocalDate dateTo
    );

    @Query("""
        SELECT COUNT(p)
        FROM PayrollEntity p
        WHERE p.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR p.subsidiary.id = :subsidiaryId)
        AND (:periodId IS NULL OR p.period.id = :periodId)
    """)
    Long countByFilters(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("periodId") Integer periodId
    );

    @Query("""
        SELECT COALESCE(SUM(p.totalNet), 0)
        FROM PayrollEntity p
        WHERE p.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR p.subsidiary.id = :subsidiaryId)
        AND (:periodId IS NULL OR p.period.id = :periodId)
    """)
    Double getTotalAmountByFilters(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("periodId") Integer periodId
    );

    @Query("""
        SELECT COUNT(p)
        FROM PayrollEntity p
        JOIN p.state s
        WHERE p.deletedAt IS NULL
        AND s.code IN ('DRAFT', 'IN_PROGRESS')
        AND (:subsidiaryId IS NULL OR p.subsidiary.id = :subsidiaryId)
        AND (:periodId IS NULL OR p.period.id = :periodId)
    """)
    Long countPendingByFilters(
            @Param("subsidiaryId") Short subsidiaryId,
            @Param("periodId") Integer periodId
    );
}
