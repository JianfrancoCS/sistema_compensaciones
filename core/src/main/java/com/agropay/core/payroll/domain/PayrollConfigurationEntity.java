package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tbl_payroll_configuration", schema = "app",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_payroll_config_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_payroll_config_code_active", columnNames = {"code"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollConfigurationEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 50)
    private String code;

    @OneToMany(mappedBy = "payrollConfiguration", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PayrollConfigurationConceptEntity> concepts = new HashSet<>(); // Inicialización directa

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}
