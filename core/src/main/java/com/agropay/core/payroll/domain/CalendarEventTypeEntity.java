package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tbl_calendar_event_types", schema = "app",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_calendar_event_types_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_calendar_event_types_code_active", columnNames = {"code"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventTypeEntity extends AbstractEntity<Short>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "eventType", fetch = FetchType.LAZY)
    private Set<CalendarEventEntity> events;
}