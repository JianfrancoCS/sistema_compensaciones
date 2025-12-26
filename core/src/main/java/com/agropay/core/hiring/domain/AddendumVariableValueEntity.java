package com.agropay.core.hiring.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tbl_addendum_variable_values", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddendumVariableValueEntity extends AbstractEntity {

    @EmbeddedId
    private AddendumVariableValueId id;

    @ManyToOne
    @MapsId("addendumId")
    @JoinColumn(name = "addendum_id")
    private AddendumEntity addendum;

    @ManyToOne
    @MapsId("variableId")
    @JoinColumn(name = "variable_id")
    private VariableEntity variable;

    private String value;
}