package com.agropay.core.hiring.domain;

import com.agropay.core.images.application.usecase.IImageable;
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
@Table(name = AddendumEntity.TABLE_NAME, schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddendumEntity extends AbstractEntity<Long> implements IImageable {

    public static final String TABLE_NAME = "tbl_addendums";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "addendum_number", nullable = false, length = 30)
    private String addendumNumber;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "new_end_date")
    private LocalDate newEndDate;

    @Column(name = "new_salary", precision = 10, scale = 2)
    private java.math.BigDecimal newSalary;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractEntity contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addendum_type_id", nullable = false)
    private AddendumTypeEntity addendumType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private AddendumTemplateEntity template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_addendum_id")
    private AddendumEntity parentAddendum;

    @OneToMany(mappedBy = "parentAddendum", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AddendumEntity> childAddendums = new HashSet<>();

    @OneToMany(mappedBy = "addendum", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AddendumVariableValueEntity> variableValues = new HashSet<>();

    @Override
    public String getSimpleName() {
        return AddendumEntity.class.getSimpleName();
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