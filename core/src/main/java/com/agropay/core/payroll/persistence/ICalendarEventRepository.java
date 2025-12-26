package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.CalendarEventEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICalendarEventRepository extends ISoftRepository<CalendarEventEntity, Integer> {

    Optional<CalendarEventEntity> findByPublicId(UUID publicId);

    @Query("SELECT ce FROM CalendarEventEntity ce JOIN FETCH ce.workCalendar wc WHERE wc.date BETWEEN :startDate AND :endDate")
    List<CalendarEventEntity> findByDateRange(LocalDate startDate, LocalDate endDate);

}
