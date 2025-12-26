package com.agropay.core.auth.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "tbl_elements", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ElementEntity extends AbstractEntity<Short> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(length = 255)
    private String route;

    @Column(length = 100)
    private String icon;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", referencedColumnName = "id")
    @ToString.Exclude
    private ContainerEntity container;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_web", nullable = false)
    private Boolean isWeb;

    @Column(name = "is_mobile", nullable = false)
    private Boolean isMobile;

    @Column(name = "is_desktop", nullable = false)
    private Boolean isDesktop;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.orderIndex == null) {
            this.orderIndex = 0;
        }
        if (this.isWeb == null) {
            this.isWeb = true; // Por defecto disponible en web
        }
        if (this.isMobile == null) {
            this.isMobile = true; // Por defecto disponible en móvil
        }
        if (this.isDesktop == null) {
            this.isDesktop = false; // Por defecto NO disponible en desktop
        }
    }
}

