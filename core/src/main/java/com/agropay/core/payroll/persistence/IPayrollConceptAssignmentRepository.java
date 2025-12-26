package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.PayrollConceptAssignmentEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPayrollConceptAssignmentRepository extends ISoftRepository<PayrollConceptAssignmentEntity, Long> {

    List<PayrollConceptAssignmentEntity> findByPayrollId(Long payrollId);

}
