package com.agropay.core.assignment.domain;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_tareos", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TareoEntity extends AbstractEntity<Integer> {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "temporal_id", nullable = false, unique = true)
    private String temporalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_employee_document_number", referencedColumnName = "person_document_number", nullable = false)
    @ToString.Exclude
    private EmployeeEntity supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_id", nullable = false)
    @ToString.Exclude
    private LaborEntity labor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = true) // NULL para tareos administrativos
    @ToString.Exclude
    private LoteEntity lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsidiary_id", nullable = false) // Relación directa con subsidiaria
    @ToString.Exclude
    private SubsidiaryEntity subsidiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanner_employee_document_number", referencedColumnName = "person_document_number")
    @ToString.Exclude
    private EmployeeEntity scanner;

    @Column(name = "closed_at")
    private LocalDateTime closedAt; // Fecha y hora en que se cerró el tareo (cuando se envía el cierre final)

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}