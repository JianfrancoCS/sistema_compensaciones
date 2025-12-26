package com.agropay.core.payroll.service.usecase;

import com.agropay.core.payroll.model.config.PayrollConceptAssignmentDTO;
import com.agropay.core.payroll.model.config.UpdatePayrollConceptsRequest;

import java.util.List;
import java.util.UUID;

public interface IPayrollConceptAssignmentService {

    /**
     * Retrieves a list of all available concepts, indicating which ones are assigned to a specific payroll.
     *
     * @param payrollPublicId The public ID of the payroll.
     * @return A list of concepts with their assignment status.
     */
    List<PayrollConceptAssignmentDTO> getConceptAssignments(UUID payrollPublicId);

    /**
     * Updates the concepts assigned to a specific payroll using a "delete and replace" strategy.
     *
     * @param payrollPublicId The public ID of the payroll to update.
     * @param request The request containing the list of concept public IDs to be assigned.
     * @return The updated list of concept assignments for the payroll.
     */
    List<PayrollConceptAssignmentDTO> updateConceptAssignments(UUID payrollPublicId, UpdatePayrollConceptsRequest request);

}
