package com.agropay.core.validation.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tbl_dynamic_variable_methods", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicVariableMethodEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dynamic_variable_id", nullable = false)
    private DynamicVariableEntity dynamicVariable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_method_id", nullable = false)
    private ValidationMethodEntity validationMethod;

    @Column(length = 100)
    private String value;

    @Column(name = "execution_order", nullable = false)
    private Integer executionOrder;
}