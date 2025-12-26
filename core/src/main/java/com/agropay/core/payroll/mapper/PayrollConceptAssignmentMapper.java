package com.agropay.core.payroll.mapper;

import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.payroll.model.config.PayrollConceptAssignmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayrollConceptAssignmentMapper {

    @Mapping(source = "concept.publicId", target = "conceptPublicId")
    @Mapping(source = "concept.category.name", target = "category")
    @Mapping(source = "assigned", target = "isAssigned")
    PayrollConceptAssignmentDTO toAssignmentDTO(ConceptEntity concept, boolean assigned);

}
