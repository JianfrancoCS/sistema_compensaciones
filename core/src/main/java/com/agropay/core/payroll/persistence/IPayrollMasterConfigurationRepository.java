package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.PayrollConfigurationEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPayrollMasterConfigurationRepository extends ISoftRepository<PayrollConfigurationEntity, Long> {

    Optional<PayrollConfigurationEntity> findByPublicId(UUID publicId);

    Optional<PayrollConfigurationEntity> findByCode(String code);

    // Since there's only one active configuration, this will return the most recent one if not soft-deleted
    Optional<PayrollConfigurationEntity> findTop1ByDeletedAtIsNullOrderByIdDesc();

}
