package com.agropay.core.validation.persistence;

import com.agropay.core.validation.domain.DynamicVariableEntity;
import com.agropay.core.validation.domain.DynamicVariableMethodEntity;
import com.agropay.core.hiring.domain.VariableEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface IDynamicVariableMethodRepository extends ISoftRepository<DynamicVariableMethodEntity, Long>, JpaSpecificationExecutor<DynamicVariableMethodEntity> {
    List<DynamicVariableMethodEntity> findByDynamicVariableOrderByExecutionOrder(DynamicVariableEntity dynamicVariable);
    void deleteByDynamicVariable(DynamicVariableEntity dynamicVariable);

    @Query("""
        SELECT v.id as variableId, COALESCE(COUNT(dvm.id), 0) as methodCount
        FROM VariableEntity v
        LEFT JOIN v.dynamicVariable dv
        LEFT JOIN dv.dynamicVariableMethods dvm
        WHERE v.id IN :variableIds
        GROUP BY v.id
        """)
    List<Map<String, Object>> countMethodsByVariableIds(@Param("variableIds") List<Short> variableIds);
}