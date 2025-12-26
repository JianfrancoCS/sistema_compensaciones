package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.PayrollPeriodEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPayrollPeriodRepository extends ISoftRepository<PayrollPeriodEntity, Integer> {

    Optional<PayrollPeriodEntity> findByPublicId(UUID publicId);

    /**
     * Finds the most recent payroll period based on the period's end date.
     * Uses multiple ordering criteria to ensure unique ordering.
     * @return The latest payroll period entity.
     */
    @Query("SELECT p FROM PayrollPeriodEntity p ORDER BY p.periodEnd DESC, p.id DESC")
    List<PayrollPeriodEntity> findTop1ByOrderByPeriodEndDescIdDesc(Pageable pageable);

    /**
     * Finds the most recent payroll period based on the period's end date.
     * @return The latest payroll period entity.
     */
    default Optional<PayrollPeriodEntity> findFirstByOrderByPeriodEndDesc() {
        List<PayrollPeriodEntity> results = findTop1ByOrderByPeriodEndDescIdDesc(Pageable.ofSize(1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Counts periods in a specific year and month that end on or before the given date.
     * Used to calculate the period number within a month (for weekly periods).
     * @param year The year
     * @param month The month
     * @param periodEnd The period end date (inclusive)
     * @return The count of periods
     */
    long countByYearAndMonthAndPeriodEndLessThanEqual(Short year, Byte month, java.time.LocalDate periodEnd);

}
