package com.agropay.core.payroll.service.usecase;

import com.agropay.core.payroll.model.masterconfig.CommandPayrollConfigurationResponse;
import com.agropay.core.payroll.model.masterconfig.CreatePayrollConfigurationRequest;
import com.agropay.core.payroll.model.masterconfig.PayrollConfigurationConceptAssignmentDTO;
import com.agropay.core.payroll.model.masterconfig.UpdateConceptAssignmentsRequest;

import java.util.List;

public interface IPayrollConfigurationMasterService {

    /**
     * Creates a new master payroll configuration. This will soft-delete any previously active configuration.
     *
     * @param request The request to create the configuration.
     * @return The details of the newly created configuration.
     */
    CommandPayrollConfigurationResponse createConfiguration(CreatePayrollConfigurationRequest request);

    /**
     * Retrieves the active master payroll configuration.
     *
     * @return The details of the active configuration.
     */
    CommandPayrollConfigurationResponse getActiveConfiguration();

    /**
     * Retrieves a list of all available concepts, indicating which ones are assigned to the active master configuration.
     *
     * @return A list of concepts with their assignment status.
     */
    List<PayrollConfigurationConceptAssignmentDTO> getConceptAssignmentsForActiveConfiguration();

    /**
     * Updates the concepts assigned to the active master payroll configuration.
     *
     * @param request The request containing the list of concept public IDs to be assigned.
     * @return The updated list of concept assignments for the configuration.
     */
    List<PayrollConfigurationConceptAssignmentDTO> updateConceptAssignmentsForActiveConfiguration(UpdateConceptAssignmentsRequest request);

    /**
     * Deletes the active master payroll configuration (soft delete).
     *
     */
    void deleteActiveConfiguration();

}
