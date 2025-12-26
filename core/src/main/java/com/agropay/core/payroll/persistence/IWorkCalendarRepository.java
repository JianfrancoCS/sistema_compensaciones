package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.WorkCalendarEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IWorkCalendarRepository extends ISoftRepository<WorkCalendarEntity, Integer> {

    Optional<WorkCalendarEntity> findByDate(LocalDate date);

    Optional<WorkCalendarEntity> findByPublicId(UUID publicId);

    /**
     * Find all working days between two dates (inclusive)
     */
    @Query("SELECT w FROM WorkCalendarEntity w " +
           "WHERE w.date BETWEEN :startDate AND :endDate " +
           "AND w.isWorkingDay = true " +
           "ORDER BY w.date ASC")
    List<WorkCalendarEntity> findWorkingDaysBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Count working days between two dates (inclusive)
     */
    @Query("SELECT COUNT(w) FROM WorkCalendarEntity w " +
           "WHERE w.date BETWEEN :startDate AND :endDate " +
           "AND w.isWorkingDay = true")
    Long countWorkingDaysBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find all calendar entries between two dates (inclusive), regardless of working day status
     */
    @Query("SELECT w FROM WorkCalendarEntity w LEFT JOIN FETCH w.events WHERE w.date BETWEEN :startDate AND :endDate ORDER BY w.date ASC")
    List<WorkCalendarEntity> findAllBetweenWithEvents(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Get all distinct year-month combinations that have calendar entries
     * Returns list of Object[] where [0] = year (Integer), [1] = month (Integer)
     */
    @Query("SELECT DISTINCT YEAR(w.date) as year, MONTH(w.date) as month " +
           "FROM WorkCalendarEntity w " +
           "ORDER BY YEAR(w.date) DESC, MONTH(w.date) ASC")
    List<Object[]> findDistinctYearMonthCombinations();

}
