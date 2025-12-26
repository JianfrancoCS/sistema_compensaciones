package com.agropay.core.assignment.domain;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_tareo_employees", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TareoEmployeeEntity extends AbstractEntity<Long> {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tareo_id", nullable = false)
    @ToString.Exclude
    private TareoEntity tareo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_document_number", referencedColumnName = "person_document_number", nullable = false)
    @ToString.Exclude
    private EmployeeEntity employee;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "actual_hours", precision = 5, scale = 2)
    private BigDecimal actualHours;

    @Column(name = "paid_hours", precision = 5, scale = 2)
    private BigDecimal paidHours;

    @Column(name = "productivity")
    private Integer productivity; // Total de unidades productivas registradas al finalizar el tareo (solo para labores de destajo)

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}