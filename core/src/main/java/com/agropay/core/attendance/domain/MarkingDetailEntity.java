package com.agropay.core.attendance.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_marking_details", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkingDetailEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marking_id", nullable = false)
    private MarkingEntity marking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marking_reason_id", nullable = false)
    private MarkingReasonEntity markingReason;

    @Column(name = "person_document_number", nullable = false, length = 15)
    private String personDocumentNumber;

    @Column(name = "is_entry", nullable = false)
    private Boolean isEntry;

    @Column(name = "is_employee", nullable = false)
    private Boolean isEmployee;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;
}