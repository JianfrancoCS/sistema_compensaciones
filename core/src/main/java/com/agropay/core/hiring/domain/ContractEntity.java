package com.agropay.core.hiring.domain;

import com.agropay.core.files.application.usecase.IFileable;
import com.agropay.core.images.application.usecase.IImageable;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = ContractEntity.TABLE_NAME, schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractEntity extends AbstractEntity<Long> implements IImageable, IFileable {

    public static final String TABLE_NAME = "tbl_contracts";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "contract_number", nullable = false, length = 30)
    private String contractNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "extended_end_date")
    private LocalDate extendedEndDate;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "person_document_number", nullable = false, length = 15)
    private String personDocumentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_type_id", nullable = false)
    private ContractTypeEntity contractType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ContractTemplateEntity template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsidiary_id")
    private SubsidiaryEntity subsidiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private PositionEntity position;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ContractVariableValueEntity> variableValues = new HashSet<>();

    @Override
    public String getSimpleName() {
        return TABLE_NAME;
    }

    // Implementación de IImageable.getId() - convierte Long a String
    @Override
    public String getId() {
        return String.valueOf(this.id);
    }
    
    // Método para obtener el ID como Long (para uso interno cuando se necesita Long)
    public Long getEntityId() {
        return this.id;
    }
}
