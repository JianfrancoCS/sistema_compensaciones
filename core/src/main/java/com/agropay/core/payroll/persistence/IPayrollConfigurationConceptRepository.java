package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.PayrollConfigurationConceptEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPayrollConfigurationConceptRepository extends ISoftRepository<PayrollConfigurationConceptEntity, Long> {

    List<PayrollConfigurationConceptEntity> findByPayrollConfigurationId(Long payrollConfigurationId);

    boolean existsByConceptId(Short conceptId);

}
