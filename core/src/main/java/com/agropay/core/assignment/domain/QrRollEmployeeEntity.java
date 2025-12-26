package com.agropay.core.assignment.domain;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tbl_qr_roll_employees", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class QrRollEmployeeEntity extends AbstractEntity<Long> {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_roll_id", nullable = false)
    @ToString.Exclude
    private QrRollEntity qrRoll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_document_number", referencedColumnName = "person_document_number", nullable = false)
    @ToString.Exclude
    private EmployeeEntity employee;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}