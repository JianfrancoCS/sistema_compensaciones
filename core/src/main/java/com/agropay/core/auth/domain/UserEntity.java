package com.agropay.core.auth.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_users", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class UserEntity extends AbstractEntity<Short> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @Column(name = "employee_id", length = 15)
    private String employeeId; // FK a tbl_employees (person_document_number) - NULL permitido

    @Column(name = "profile_id")
    private Short profileId; // FK a tbl_profiles (perfil actual del usuario)

    @Column(nullable = false, length = 100, unique = true)
    private String username; // Username único (normalmente el DNI)

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Hash de la contraseña (bcrypt)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}

