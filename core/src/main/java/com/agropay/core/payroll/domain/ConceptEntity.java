package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tbl_concepts", schema = "app",
    indexes = {
        @Index(name = "IX_concepts_category", columnList = "category_id, deleted_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_concepts_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_concepts_code_active", columnNames = {"code"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConceptEntity extends AbstractEntity<Short> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ConceptCategoryEntity category;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "calculation_priority", nullable = false)
    @Builder.Default
    private Short calculationPriority = 100;

    @OneToMany(mappedBy = "concept", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PayrollConfigurationConceptEntity> payrollConfigurations;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}