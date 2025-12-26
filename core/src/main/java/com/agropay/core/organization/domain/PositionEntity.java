package com.agropay.core.organization.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "tbl_positions", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PositionEntity extends AbstractEntity<Short> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false)
    private String name;

    @Column(name = "requires_manager", nullable = false)
    private boolean requiresManager;

    @Column(name = "is_unique", nullable = false)
    private boolean unique;

    @Column(name = "salary", precision = 10, scale = 2)
    private java.math.BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    @ToString.Exclude
    private AreaEntity area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_position_id", referencedColumnName = "id")
    @ToString.Exclude
    private PositionEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_manager_position_id", referencedColumnName = "id")
    @ToString.Exclude
    private PositionEntity requiredManagerPosition;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}
