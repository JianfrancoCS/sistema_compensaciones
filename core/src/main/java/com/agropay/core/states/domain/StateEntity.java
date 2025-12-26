package com.agropay.core.states.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tbl_states", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StateEntity extends AbstractEntity<Short> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "public_id")
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private DomainEntity domain;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}
