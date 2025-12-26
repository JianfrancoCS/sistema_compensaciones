package com.agropay.core.hiring.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tbl_contract_template_variables", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTemplateVariableEntity extends AbstractEntity<Short> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Short id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_template_id", nullable = false)
    private ContractTemplateEntity contractTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variable_id", nullable = false)
    @EqualsAndHashCode.Include
    private VariableEntity variable;

    @Column(name = "is_required", nullable = false)
    @EqualsAndHashCode.Include
    private Boolean isRequired;

    @Column(name = "display_order", nullable = false)
    @EqualsAndHashCode.Include
    private Integer displayOrder;
}