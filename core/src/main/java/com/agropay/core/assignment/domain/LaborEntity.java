package com.agropay.core.assignment.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tbl_labors", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class LaborEntity extends AbstractEntity<Short> {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "min_task_requirement", precision = 10, scale = 2)
    private BigDecimal minTaskRequirement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labor_unit_id", nullable = false)
    @ToString.Exclude
    private LaborUnitEntity laborUnit;

    @Column(name = "is_piecework", nullable = false)
    private Boolean isPiecework = false;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}