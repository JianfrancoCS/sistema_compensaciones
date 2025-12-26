package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "tbl_calendar_events", schema = "app",
    indexes = {
        @Index(name = "IX_calendar_events_event_type", columnList = "event_type_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_calendar_events_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_calendar_events_work_calendar_event_type_active", columnNames = {"work_calendar_id", "event_type_id"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventEntity extends AbstractEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_calendar_id", nullable = false)
    private WorkCalendarEntity workCalendar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    private CalendarEventTypeEntity eventType;

    @Column(length = 255)
    private String description;
}