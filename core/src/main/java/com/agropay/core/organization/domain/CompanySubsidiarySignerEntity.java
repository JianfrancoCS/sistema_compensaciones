package com.agropay.core.organization.domain;

import com.agropay.core.files.application.usecase.IFileable;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * Entidad histórica para responsables de firma de boletas de pago
 * Permite tener diferentes responsables por subsidiaria
 * El último registro (más reciente) será el que se use para la boleta
 * Si subsidiary_id es NULL, el responsable es a nivel de empresa
 */
@Entity
@Table(name = CompanySubsidiarySignerEntity.TABLE_NAME, schema = "app",
    indexes = {
        @Index(name = "IX_company_subsidiary_signers_company_subsidiary", 
               columnList = "company_id, subsidiary_id, created_at"),
        @Index(name = "IX_company_subsidiary_signers_public_id", 
               columnList = "public_id"),
        @Index(name = "IX_company_subsidiary_signers_employee", 
               columnList = "responsible_employee_document_number")
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySubsidiarySignerEntity extends AbstractEntity<Long> implements IFileable {

    public static final String TABLE_NAME = "tbl_company_subsidiary_signers";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    private CompanyEntity company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsidiary_id", nullable = true)
    @ToString.Exclude
    private SubsidiaryEntity subsidiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_employee_document_number", 
               referencedColumnName = "person_document_number", 
               nullable = false)
    @ToString.Exclude
    private EmployeeEntity responsibleEmployee;

    @Column(name = "responsible_position", nullable = false, length = 100)
    @Builder.Default
    private String responsiblePosition = "JEFE DE RECURSOS HUMANOS";

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "signature_image_url", length = 500)
    private String signatureImageUrl;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

    @Override
    public String getSimpleName() {
        return TABLE_NAME;
    }

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }
}

