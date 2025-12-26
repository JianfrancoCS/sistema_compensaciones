package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tbl_payroll_configuration_concepts", schema = "app",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_payroll_config_concepts_active", columnNames = {"payroll_configuration_id", "concept_id"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollConfigurationConceptEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true) // Added for consistency
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_configuration_id", nullable = false)
    private PayrollConfigurationEntity payrollConfiguration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id", nullable = false)
    private ConceptEntity concept;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}
