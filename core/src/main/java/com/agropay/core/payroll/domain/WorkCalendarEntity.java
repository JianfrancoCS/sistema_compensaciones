package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tbl_work_calendar", schema = "app",
    indexes = {
        @Index(name = "IX_work_calendar_is_working_day", columnList = "is_working_day, date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_work_calendar_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_work_calendar_date_active", columnNames = {"date"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCalendarEntity extends AbstractEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "is_working_day", nullable = false)
    private Boolean isWorkingDay;

    @OneToMany(mappedBy = "workCalendar", fetch = FetchType.LAZY)
    private Set<CalendarEventEntity> events;


    public boolean isWorkingDay() {
        return Boolean.TRUE.equals(this.isWorkingDay);
    }
    public boolean hasEventType(String eventTypeCode) {
        if (events == null || events.isEmpty()) {
            return false;
        }
        return events.stream()
            .anyMatch(event -> event.getEventType() != null
                && eventTypeCode.equals(event.getEventType().getCode()));
    }

    public boolean isHoliday() {
        return hasEventType("HOLIDAY");
    }

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}