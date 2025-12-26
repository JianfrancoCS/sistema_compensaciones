package com.agropay.core.payroll.service.usecase;

import com.agropay.core.payroll.model.period.CommandPayrollPeriodResponse;
import com.agropay.core.payroll.model.period.CreatePayrollPeriodRequest;
import com.agropay.core.payroll.model.period.PayrollPeriodSelectOptionDTO;

import java.util.List;
import java.util.UUID;

public interface IPayrollPeriodService {

    /**
     * Creates a new payroll period.
     * If the request contains an explicit start date, a new period is started from that date.
     * If the request is empty, a continuous period is created based on the last one.
     *
     * @param request The request object, which may contain an explicit start date.
     * @return The details of the newly created payroll period.
     */
    CommandPayrollPeriodResponse createPeriod(CreatePayrollPeriodRequest request);

    /**
     * Retrieves a list of all existing payroll periods.
     *
     * @return A list of payroll period details.
     */
    List<CommandPayrollPeriodResponse> getAllPeriods();

    /**
     * Retrieves a list of payroll periods formatted for select/dropdown options.
     * Includes a flag indicating if the period is already assigned to an active payroll.
     *
     * @return A list of PayrollPeriodSelectOptionDTO.
     */
    List<PayrollPeriodSelectOptionDTO> getSelectOptions();

    /**
     * Deletes a payroll period by its public ID (soft delete).
     *
     * @param publicId The public ID of the payroll period to delete.
     */
    void deletePeriod(UUID publicId);

}
