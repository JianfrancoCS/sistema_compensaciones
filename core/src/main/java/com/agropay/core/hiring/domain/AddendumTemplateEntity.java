package com.agropay.core.hiring.domain;

import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = AddendumTemplateEntity.TABLE_NAME, schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddendumTemplateEntity extends AbstractEntity<Short> {

    public static final String TABLE_NAME = "tbl_addendum_templates";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "template_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String templateContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addendum_type_id", nullable = false)
    private AddendumTypeEntity addendumType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity state;

    @OneToMany(mappedBy = "addendumTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction(value = "deleted_at IS NULL")
    @Builder.Default
    private Set<AddendumTemplateVariableEntity> variables = new HashSet<>();
}