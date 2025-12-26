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
@Table(name = "tbl_contract_variable_values", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractVariableValueEntity extends AbstractEntity {

    @EmbeddedId
    private ContractVariableValueId id;

    @ManyToOne
    @MapsId("contractId")
    @JoinColumn(name = "contract_id")
    private ContractEntity contract;

    @ManyToOne
    @MapsId("variableId")
    @JoinColumn(name = "variable_id")
    private VariableEntity variable;

    private String value;
}
