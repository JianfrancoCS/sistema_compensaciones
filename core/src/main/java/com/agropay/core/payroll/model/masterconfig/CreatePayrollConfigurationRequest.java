package com.agropay.core.payroll.model.masterconfig;

import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new master payroll configuration.
 */
public record CreatePayrollConfigurationRequest(
    List<UUID> conceptsPublicIds
) {}
