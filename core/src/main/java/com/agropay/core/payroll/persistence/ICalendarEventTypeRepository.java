package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.CalendarEventTypeEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICalendarEventTypeRepository extends ISoftRepository<CalendarEventTypeEntity, Short> {

    Optional<CalendarEventTypeEntity> findByCode(String code);

    Optional<CalendarEventTypeEntity> findByPublicId(UUID publicId);

}
